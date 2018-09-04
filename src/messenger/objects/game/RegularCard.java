package messenger.objects.game;

public class RegularCard extends Card {

    public RegularCard (int num) {
        this.setValue(num);
        this.setType(CardType.REGULAR);
    }

    @Override
    public void action(Game game, Player player) {
        game.getDiscarded().push(this);
    }
}
