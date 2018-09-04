package messenger.objects.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Game {
    private LinkedList<Card> deck;
    private LinkedList<Card> discarded;

    private ArrayList<Player> players;
    private int playerTurn = 0;
    private boolean reversed = false;

    public Game () {
        players = new ArrayList<>();
        deck = new LinkedList<>();
        discarded = new LinkedList<>();

        generateDeck();
    }

    public int find (Player p) {
        for (int i = 0; i < players.size(); i++)
            if (players.get(i).getName().equals(p.getName()))
                return i;

        return -1;
    }

    private boolean isPlayersTurn (String name) {
        assert players.get(playerTurn) != null;

        return players.get(playerTurn).getName().equals(name);
    }

    public void nextTurn () {
        if (reversed && --playerTurn <= -1)
            playerTurn = players.size() - 1;
        else if (++playerTurn >= players.size())
            playerTurn = 0;
    }

    public void draw (int playerIndex, int num) {
        if (playerIndex >= 0 && playerIndex < players.size())
            for (int i = 0; i < num; i++)
                players.get(playerIndex).getCards().add(deck.pop());
    }

    public void reverse () {
        this.reversed = !this.reversed;
    }

    private void generateDeck () {
        Random random = new Random();
        Card card;

        for (int i = 0; i < 30; i++) {
            CardType type = (random.nextInt(100) > 50) ? CardType.ACTION : CardType.REGULAR;

            if (type == CardType.REGULAR)
                card = new RegularCard(random.nextInt(8) + 1);
            else {
                switch (random.nextInt(2) + 1) {
                    case 1: // PLUS
                        card = new PlusCard((random.nextInt(100) > 75) ? 4 : 2);
                        break;
                    case 2: // REVERSE
                        card = new ReverseCard();
                        break;
                    default: // STOP
                        card = new StopCard();
                }
            }

            deck.push(card);
        }
    }

    public void setDeck(LinkedList<Card> deck) {
        this.deck = deck;
    }

    public void setDiscarded(LinkedList<Card> discarded) {
        this.discarded = discarded;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public void setPlayerTurn(int playerTurn) {
        this.playerTurn = playerTurn;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    public LinkedList<Card> getDeck() {
        return deck;
    }

    public LinkedList<Card> getDiscarded() {
        return discarded;
    }
}
