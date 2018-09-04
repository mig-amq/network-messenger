package messenger.objects.game;

public class PlusCard extends Card {

    private int add = 0;

    public PlusCard (int num) {
        this.add = num;
        this.setValue(1);
        this.setType(CardType.ACTION);
    }

    @Override
    public void action(Game game, Player player) {
        game.draw(game.find(player), this.add);
    }
}
