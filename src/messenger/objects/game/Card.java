package messenger.objects.game;

public abstract class Card {
    private int value;
    private CardType type;

    public Card () {
        this.setValue(1);
        this.setType(CardType.REGULAR);
    }

    public abstract void action(Game game, Player player);

    public void setType(CardType type) {
        this.type = type;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
