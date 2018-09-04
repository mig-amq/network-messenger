package messenger.objects.game;

public class ReverseCard extends Card {

    public ReverseCard () {
        this.setValue(2);
        this.setType(CardType.ACTION);
    }

    @Override
    public void action(Game game, Player player) {
        game.reverse();
    }
}
