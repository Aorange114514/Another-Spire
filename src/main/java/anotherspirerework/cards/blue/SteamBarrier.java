package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Frost;

public class SteamBarrier extends AbstractCard {
    public static final String ID = "Steam";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SteamBarrier() {
        super(ID, cardStrings.NAME, "blue/skill/steam_barrier", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.COMMON, CardTarget.SELF);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
        this.cardsToPreview = new Dazed();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new ChannelAction(new Frost()));
        }
        addToBot(new MakeTempCardInDrawPileAction(new Dazed(), 1, true, true));
    }

    /**
     * Upgraded: 2 Frost orbs and the Dazed. Exhaust is gone at both levels, so the description never
     * mentions it and the magic number refreshes itself, with no UPGRADE_DESCRIPTION needed.
     */
    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new SteamBarrier();
    }
}
