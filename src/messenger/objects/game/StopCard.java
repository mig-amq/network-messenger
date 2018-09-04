package messenger.objects.game;

public class StopCard extends Card {

    public StopCard () {
        this.setValue(3);
        this.setType(CardType.ACTION);
    }

    @Override
    public void action(Game game, Player player) {
        game.nextTurn();
    }
}
