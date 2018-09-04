package messenger.objects.game;

import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Card> cards;

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public String getName() {
        return name;
    }
}
