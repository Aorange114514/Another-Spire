param([switch]$CheckJar)
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$root = Join-Path $project 'src\main\resources\anotherspirerework\localization'
function Assert($condition, $message) { if (-not $condition) { throw $message } }
function Json($path) { Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json }
foreach ($type in @('CardStrings', 'PowerStrings', 'RelicStrings')) {
    $eng = Json (Join-Path $root "eng\$type.json")
    $zhs = Json (Join-Path $root "zhs\$type.json")
    Assert (@(Compare-Object @($eng.psobject.Properties.Name) @($zhs.psobject.Properties.Name)).Count -eq 0) "$type language keys differ"
    foreach ($entry in $eng.psobject.Properties) {
        $other = $zhs.psobject.Properties[$entry.Name].Value
        foreach ($field in $entry.Value.psobject.Properties) {
            Assert ($null -ne $other.psobject.Properties[$field.Name]) "Missing Chinese field: $($entry.Name) $($field.Name)"
            $a = @([regex]::Matches((@($field.Value) -join ' '), '!\w+!|\[[A-Z]\]') | ForEach-Object {$_.Value}) -join '|'
            $b = @([regex]::Matches((@($other.psobject.Properties[$field.Name].Value) -join ' '), '!\w+!|\[[A-Z]\]') | ForEach-Object {$_.Value}) -join '|'
            Assert ($a -ceq $b) "Dynamic/energy tokens differ: $($entry.Name) $($field.Name)"
        }
    }
}
$patch = Get-Content (Join-Path $project 'src\main\java\anotherspirerework\patches\CardSwapPatch.java') -Raw
$list = @([regex]::Matches($patch, 'new (\w+)\(\)') | ForEach-Object {$_.Groups[1].Value})
Assert ($list.Count -eq 96) 'Expected 96 replacements'
Assert (@($list | Select-Object -Unique).Count -eq 96) 'Duplicate replacement'
$cards = Json (Join-Path $root 'eng\CardStrings.json')
$zhCards = Json (Join-Path $root 'zhs\CardStrings.json')
foreach ($langCards in @($cards, $zhCards)) {
    $berserk = $langCards.psobject.Properties['${modID}:Berserk'].Value
    Assert (-not ($berserk.DESCRIPTION -match 'Vulnerable|\u6613\u4f24')) 'Berserk must not apply Vulnerable'
    $seeing = $langCards.psobject.Properties['${modID}:Seeing Red'].Value
    Assert ([regex]::Matches($seeing.DESCRIPTION, '\[R\]').Count -eq 3) 'Seeing Red must grant 3 energy'
    $concentrate = $langCards.psobject.Properties['${modID}:Concentrate'].Value
    Assert ([regex]::Matches($concentrate.DESCRIPTION, '\[G\]').Count -eq 2) 'Concentrate must grant 2 energy'
    Assert ([regex]::Matches($concentrate.UPGRADE_DESCRIPTION, '\[G\]').Count -eq 3) 'Concentrate+ must grant 3 energy'
    foreach ($id in @('Die Die Die', 'Envenom', 'Cloak And Dagger')) {
        Assert ($langCards.psobject.Properties['${modID}:' + $id].Value.DESCRIPTION -match '\*(Shiv|\u5c0f\u5200)') "Missing Shiv keyword association: $id"
    }
    Assert ($langCards.psobject.Properties['${modID}:ThirdEye'].Value.DESCRIPTION -match '\*(Insight|\u6d1e\u89c1)') 'Missing Insight keyword association'
}
# The buffs added with Die Die Die and Envenom keep the card name as their title, and Reprogram is a
# Power card now, so it must not advertise Exhaust anywhere. Steam Barrier is the other way round:
# it Exhausts at both levels (the requirements list Exhaust once, with no "remove it when upgraded"),
# so its upgrade text has to keep saying so.
$byLang = @{ eng = $cards; zhs = $zhCards }
foreach ($lang in @('eng', 'zhs')) {
    $powers = Json (Join-Path $root "$lang\PowerStrings.json")
    $langCards = $byLang[$lang]
    foreach ($pair in @(@('ShivPoison', 'Envenom'), @('ShivAllEnemies', 'Die Die Die'))) {
        $powerName = $powers.psobject.Properties['${modID}:' + $pair[0]].Value.NAME
        $cardName = $langCards.psobject.Properties['${modID}:' + $pair[1]].Value.NAME
        Assert ($powerName -ceq $cardName) "$lang power title must be the card name: $($pair[0])"
    }
    $reprogram = $langCards.psobject.Properties['${modID}:Reprogram'].Value.DESCRIPTION
    Assert (-not ($reprogram -match 'Exhaust|\u6d88\u8017')) "Reprogram must not advertise Exhaust: $lang"
    $steam = $langCards.psobject.Properties['${modID}:Steam'].Value
    Assert ($steam.UPGRADE_DESCRIPTION -match 'Exhaust|\u6d88\u8017') "Steam Barrier+ must advertise Exhaust: $lang"
}
# Every power exposes its strings as a static field named powerStrings: the power list screens read
# that name by reflection, and a power without it shows an unresolved title.
foreach ($power in Get-ChildItem (Join-Path $project 'src\main\java\anotherspirerework\powers') -Filter '*.java') {
    $text = Get-Content $power.FullName -Raw
    if ($text -notmatch 'extends AbstractPower') { continue }
    Assert ($text -cmatch 'PowerStrings\s+powerStrings') "$($power.Name) must declare a static powerStrings field"
}


$sources = Get-ChildItem (Join-Path $project 'src\main\java') -Recurse -Filter '*.java'
$required = @('ModTheSpire.json', 'anotherspirerework/AnotherSpireRework.class')
foreach ($name in $list) {
    $file = @($sources | Where-Object {$_.BaseName -ceq $name -and $_.FullName -match '\\cards\\'})
    Assert ($file.Count -eq 1) "Missing/ambiguous card source: $name"
    $text = Get-Content $file[0].FullName -Raw
    $id = [regex]::Match($text, 'String ID = "([^"]+)"').Groups[1].Value
    Assert ($null -ne $cards.psobject.Properties['${modID}:' + $id]) "Missing strings: $id"
}
foreach ($file in $sources) {
    $text = Get-Content $file.FullName -Raw
    Assert (-not ($text -match '\banotherspire\.|\bAnotherSpire\b')) "Stale package: $($file.FullName)"
    $relative = $file.FullName.Substring((Join-Path $project 'src\main\java').Length + 1).Replace('\', '/')
    $required += $relative.Replace('.java', '.class')
}
Get-ChildItem (Join-Path $project 'src\main\resources') -Recurse -Filter '*.json' | ForEach-Object {$null = Json $_.FullName}
# A postfix that returns a value is handed the original result as its FIRST parameter, by position
# rather than by name. Declaring the instance first makes the hook return the source object instead
# of the copy, which makes combat cards share their object with the master deck.
foreach ($source in (Get-ChildItem (Join-Path $project 'src\main\java') -Recurse -Filter '*.java')) {
    $sourceText = Get-Content $source.FullName -Raw
    foreach ($hook in [regex]::Matches($sourceText, '@SpirePostfixPatch\s+public\s+static\s+([\w\.<>\[\]]+)\s+(\w+)\s*\(([^)]*)\)')) {
        if ($hook.Groups[1].Value -eq 'void') { continue }
        $firstParam = ($hook.Groups[3].Value -split ',')[0].Trim()
        $paramName = ($firstParam -split '\s+')[-1]
        Assert ($paramName -eq '__result') "$($source.Name): a value returning postfix must declare __result first, found '$firstParam'"
    }
}
# Card art must be one of the two sizes the game draws in: the 250x190 face and the 500x380
# zoomed view. Anything else would be stretched in a way the artist did not intend.
Add-Type -AssemblyName System.Drawing
$artRoot = Join-Path $project 'src\main\resources\anotherspirerework\images'
$knownArtNames = @{}
foreach ($cardSource in Get-ChildItem (Join-Path $project 'src\main\java\anotherspirerework\cards') -Recurse -Filter '*.java') {
    $cardText = Get-Content $cardSource.FullName -Raw
    $cardId = [regex]::Match($cardText, 'String ID = "([^"]+)"').Groups[1].Value
    $asset = [regex]::Match($cardText, 'super\([^,]+,[^,]+, *"([^"]+)"').Groups[1].Value
    if (-not $cardId -or -not $asset) { continue }
    foreach ($alias in @($cardId.Replace(' ', ''), $asset.Replace('/', '_'), ($asset -split '/')[-1])) {
        $knownArtNames[$alias] = $cardSource.BaseName
    }
}
foreach ($image in Get-ChildItem $artRoot -Recurse -Filter '*.png') {
    if ($image.BaseName -in @('badge', 'missing')) { continue }
    $bitmap = [System.Drawing.Image]::FromFile($image.FullName)
    try {
        $size = "$($bitmap.Width)x$($bitmap.Height)"
        Assert ($size -eq '250x190' -or $size -eq '500x380') "Card art must be 250x190 or 500x380: $($image.Name) is $size"
    } finally { $bitmap.Dispose() }
    $alias = $image.BaseName -replace '_p$', ''
    if (-not $knownArtNames.ContainsKey($alias)) {
        Write-Warning "Card art is ignored, no card in this mod looks for '$alias': $($image.Name)"
    } elseif ($knownArtNames.Count -gt 0) {
        Write-Output "Card art '$($image.Name)' -> $($knownArtNames[$alias])"
    }
}
if ($CheckJar) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $jar = [IO.Compression.ZipFile]::OpenRead((Join-Path $project 'target\anotherspirerework.jar'))
    try {
        $names = @($jar.Entries | ForEach-Object {$_.FullName})
        foreach ($entry in $required) { Assert ($names -contains $entry) "Missing JAR entry: $entry" }
        foreach ($entry in ($jar.Entries | Where-Object {$_.FullName.EndsWith('.json')})) {
            $reader = New-Object IO.StreamReader($entry.Open())
            try {
                $text = $reader.ReadToEnd()
                Assert (-not ($text.Contains('${') -or $text.Contains('!(project.description)'))) "Unfiltered JSON: $($entry.FullName)"
                $null = $text | ConvertFrom-Json
            } finally { $reader.Dispose() }
        }
    } finally { $jar.Dispose() }
}
Write-Output 'PASS: JSON, bilingual fields/tokens, 96 unique replacements, localization coverage, source references and requested JAR checks.'