package net.marcuswhybrow.uni.g52gui.cw1.model;

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.Timer;
import net.marcuswhybrow.uni.g52gui.cw1.Card;

/**
 *
 * @author marcus
 */
public class GameModel
{
	private static GameModel gameModel = new GameModel();

	/** The number of pairs of cards (e.g. 10 pairs = 20 cards) */
	private int numberOfPairs = 10;
	/** The time in milliseconds to leave non-matched cards FACEUP */
	public static final int MISS_TIMEOUT_PERIOD = 1000;
	/** The title of the game window */
	private String title = "The Concentraiton Game";
	/** The location of the text file which lists the potential images */
	public static final String IMAGE_LIST_FILE = "images/list.txt";

	/** A list of all the cards currently in use */
	private ArrayList<Card> cards = new ArrayList<Card>();
	/** The first card chosen by the player for a potential match */
	private Card firstPickedCard = null;
	/** The second card chosen by the player for a match */
	private Card secondPickedCard = null;

	/** The text displayed on the shuffle button in the middle of a game */
	public static final String SHUFFLE_NORMAL_TEXT = "Shuffle";
	/** The text displayed on the shuffle button once a game is over */
	public static final String SHUFFLE_RESTART_TEXT = "Start Again";

	/** The possible outcomes when two cards are selected by the player */
	public static enum MatchType {HIT, MISS}

	/** The possible states which the game may be in */
	public static enum State {
		// There are no Cards FACEUP whih have not already been MATCHED
		WAITING_FOR_FIRST_CARD,
		// There is a single Card FACEUP
		WAITING_FOR_SECOND_CARD,
		// A preiod of time where the player sees both the cards but after a
		// certain period (or if the player continues choosing cards) they
		// return to being facedown
		NOTIFYING_INCORRECT_MATCH,
		// Currently this state transitions into WAITING_FOR_FIRST_CARD
		// instantly since cards remain on the game surface once MATCHED.
		NOTIFYING_CORRECT_MATCH,
		// Showing the player that they have completed the game
		NOTIFYING_COMPLETE,
		// The solve button was pressed, the cards must be shuffled to continue
		SOLVED
	}
	/** The state the game is currently in */
	private State state = State.WAITING_FOR_FIRST_CARD;

	/**
	 * A swing timer which fires an event a fixed time after an icorrect match
	 * in order to allow a period of leaving the cards face up, but eventually
	 * turning them back to be face down
	 */
	private Timer incorrectMatchTimeout;

	/**
	 * The ActionCommand String used to indicate the incorrect match timeout
	 * event.
	 */
	public static final String INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND = "incorrect_timeout";
	/** The ActionCommand String used to indicate the shuffle action */
	public static final String SHUFFLE_ACTION_COMMAND = "shuffle";
	/** The ActionCommand String used to indicate the solve action */
	public static final String SOLVE_ACTION_COMMAND = "solve";

	/** The number of times in the round a match was detected */
	private int hits = 0;
	/** The number of times in the round a miss was detected */
	private int misses = 0;

	private Dimension minimumSize = new Dimension(400, 200);

	private ArrayList<ModelListener> modelListeners = new ArrayList<ModelListener>();

	private GameModel() {}

	public static GameModel get()
	{
		return gameModel;
	}

	public void modelHasChanged()
	{
		for (ModelListener ml : modelListeners)
			ml.modelHasChanged();
	}

	public void addModelListener(ModelListener ml)
	{
		modelListeners.add(ml);
	}

	public void removeModelListener(ModelListener ml)
	{
		modelListeners.remove(ml);
	}
	
	// Setters
	// =========================================================================
	
	public void setFirstPickedCard(Card card)
	{
		firstPickedCard = card;
	}

	public void setSecondPickedCard(Card card)
	{
		secondPickedCard = card;
	}

	public void setHits(int hits)
	{
		this.hits = hits;
	}

	public void setMisses(int misses)
	{
		this.misses = misses;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	// Getters
	// =========================================================================

	public String getTitle()
	{
		return title;
	}

	public Dimension getMinimumSize()
	{
		return minimumSize;
	}

	public int getNumberOfPairs()
	{
		return numberOfPairs;
	}

	public State getState()
	{
		return state;
	}

	public Card getFirstPickedCard()
	{
		return firstPickedCard;
	}

	public Card getSecondPickedCard()
	{
		return secondPickedCard;
	}

	public ArrayList<Card> getCards()
	{
		return cards;
	}

	public int getHits()
	{
		return hits;
	}

	public int getMisses()
	{
		return misses;
	}
}
