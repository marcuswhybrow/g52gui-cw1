package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The Game class controls the mechanisms of the game and is also the class
 * which responds to Swing events such as mouse clicks etc.
 *
 * @author Marcus Whybrow
 */
public class Game extends JFrame implements ActionListener
{
	/** The number of pairs of cards (e.g. 10 pairs = 20 cards) */
	private int numberOfPairs = 10;
	/** The time in milliseconds to leave non-matched cards FACEUP */
	private int missTimeoutPeriod = 1000;
	/** The title of the game window */
	private String title = "The Concentraiton Game";
	/** The location of the text file which lists the potential images */
	private String imageListFile = "images/list.txt";
	
	/** A list of all the cards currently in use */
	private ArrayList<Card> cards = new ArrayList<Card>();
	/** The first card chosen by the player for a potential match */
	private Card firstPickedCard = null;
	/** The second card chosen by the player for a match */
	private Card secondPickedCard = null;

	/** A panel which holds all the cards in a grid layout */
	private JPanel cardPanel = new JPanel();
	/** A panel below the cards which holds the game controls and information */
	private JPanel bottomPanel = new JPanel();
	/** A panel containing the game controls */
	private JPanel controlPanel = new JPanel();
	/** A panel containing the hit and miss scores */
	private JPanel scorePanel = new JPanel();

	/** A game controlling button which turns every card faceup */
	private JButton solve = new JButton("Solve");

	/** The text displayed on the shuffle button in the middle of a game */
	private String SHUFFLE_NORMAL_TEXT = "Shuffle";
	/** The text displayed on the shuffle button once a game is over */
	private String SHUFFLE_RESTART_TEXT = "Start Again";
	/** A game controlling button which starts the round again with new cards */
	private JButton shuffle = new JButton(SHUFFLE_NORMAL_TEXT);

	/** A label which displays the current number of matches (or hits) */
	private JLabel hitsLabel = new JLabel("HITS: 0");
	/** A label which displays the current number of misses */
	private JLabel missesLabel = new JLabel("MISSES: 0");

	/** The possible outcomes when two cards are selected by the player */
	private enum MatchType {HIT, MISS}

	/** The possible states which the game may be in */
	private enum State {
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
	 * A list of relative paths to all the images which may be used as graphics
	 * for the cards
	 */
	private ArrayList<String> imageList = new ArrayList<String>();

	/**
	 * The ActionCommand String used to indicate the incorrect match timeout
	 * event.
	 */
	private String INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND = "incorrect_timeout";
	/** The ActionCommand String used to indicate the shuffle action */
	private String SHUFFLE_ACTION_COMMAND = "shuffle";
	/** The ActionCommand String used to indicate the solve action */
	private String SOLVE_ACTION_COMMAND = "solve";

	/** The number of times in the round a match was detected */
	private int hits = 0;
	/** The number of times in the round a miss was detected */
	private int misses = 0;


	/** A random number generator used to randomly select images for new Cards */
	Random rand = new Random();
	/** An array of ImageIcons which will be used used by the new set of Cards */
	ImageIcon[] imagesToBeUsed;
	/** A list of which images (by index) have been used thus far, as to avoid duplicates */
	ArrayList<Integer> indexesUsed;

	/**
	 * The constructor of the game sets up all the initial values for variables,
	 * and lays out the components which represent the game.
	 */
	public Game()
	{
		// Set the tile of the window and the size to roughly accomodate the
		// number of cards in use. Then place the window in the center of the
		// screen and tell the program to exit when the close button on this
		// window is clicked.
		setTitle(title);
		setMinimumSize(new Dimension(400, 200));
		setSize((numberOfPairs/4) * 140, 300);
		setLocation(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Now its time to layout the components, set the window to border layout
		// allowing the cards to take up the centeral space and the bottom panel
		// (housing the controls and stats) to stay at the bottom
		setLayout(new BorderLayout());

		JPanel cardPaddingPanel = new JPanel();
		cardPaddingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
		cardPaddingPanel.setLayout(new BorderLayout());
		add(cardPaddingPanel, BorderLayout.CENTER);

		cardPaddingPanel.add(cardPanel, BorderLayout.CENTER);
		cardPanel.setLayout(new GridLayout(4, numberOfPairs/4));
		cardPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));

		// Add the panel which will house the controls and stats to the bottom
		// of the frame and specify a border layout for it also
		add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout());

		// To the bottom panel add the controlPanel to the left hand side,
		// specify a flow layout which to accommodate the two buttons (shuffle and
		// solve). Then add a some visual padding for the buttons.
		bottomPanel.add(controlPanel, BorderLayout.WEST);
		controlPanel.setLayout(new FlowLayout());
		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));

		// Next add the solve button to the controlPanel, set its ActionCommand,
		// and specify that the Game class is the listener
		controlPanel.add(solve);
		solve.setActionCommand(SOLVE_ACTION_COMMAND);
		solve.addActionListener(this);

		// Next add the shuffle button to the controlPanel, set its ActionCommand
		// and specify that the Game class is the listener
		controlPanel.add(shuffle);
		shuffle.setActionCommand(SHUFFLE_ACTION_COMMAND);
		shuffle.addActionListener(this);

		// Now onto the right had side of the bottom panel add the scorePanel
		// specify a flow layout which can accommodate the two score labels
		// and add some padding
		bottomPanel.add(scorePanel, BorderLayout.EAST);
		scorePanel.setLayout(new FlowLayout());
		scorePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 30));

		// Then add both the labels to the scorePanel
		scorePanel.add(hitsLabel);
		scorePanel.add(missesLabel);


		// Create the timer for flipping non-matched cards back to FACEDOWN
		// ensuring that it only fires once and does not repeat and settings
		// the ActionCommand such that the event is discernible from the others
		incorrectMatchTimeout = new Timer(missTimeoutPeriod, this);
		incorrectMatchTimeout.setRepeats(false);
		incorrectMatchTimeout.setActionCommand(INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND);

		// Get the list of possible images which can be attributed to a Card
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(imageListFile)));
		String line;
		try
		{
			while (null != (line = br.readLine()))
				imageList.add(line);
		}
		catch (IOException ex)
		{
			System.err.println("Couldn't read the list of images");
			System.exit(-1);
		}

		// Now that the list of images has been populated we can choose some
		// card whith which to start the game
		getNewCards();

		// and then display the window when the game can begin
		setVisible(true);
	}

	/**
	 * The method with is called when an event is fired from an object which
	 * this object is an ActionListener for
	 *
	 * @param e The ActionEvent containing information regarding the event
	 */
	public void actionPerformed(ActionEvent e)
	{
		// The source which fired the event
		Object source = e.getSource();

		// The solve button has been pressed
		if (areEqual(e.getActionCommand(), SOLVE_ACTION_COMMAND))
			changeState(State.SOLVED);

		// The shuffle button has been pressed
		else if (areEqual(e.getActionCommand(), SHUFFLE_ACTION_COMMAND))
			getNewCards();

		// The non-matched cards timeout has occurred
		else if (areEqual(e.getActionCommand(), INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND) && state == State.NOTIFYING_INCORRECT_MATCH)
			changeState(State.WAITING_FOR_FIRST_CARD);

		// A Card has been chosen
		else if (source instanceof Card)
		{
			Card card = (Card) source;
			switch (state)
			{
				case WAITING_FOR_FIRST_CARD:
					if (card.turnToFaceUp())
					{
						firstPickedCard = card;
						changeState(State.WAITING_FOR_SECOND_CARD);
					}
					break;

				case WAITING_FOR_SECOND_CARD:
					if (card.turnToFaceUp())
					{
						secondPickedCard = card;
						checkChosenCards();
					}
					break;
				case NOTIFYING_INCORRECT_MATCH:
					if (card.turnToFaceUp())
					{
						incorrectMatchTimeout.stop();
						clearUnmatchedFaceUpCards();
						firstPickedCard = card;
						changeState(State.WAITING_FOR_SECOND_CARD);
					}
			}
		}
	}

	/**
	 * Checks to see if the second picked Card is paried with the first Card,
	 * and progresses the game based upon the the result
	 */
	private void checkChosenCards()
	{
		if (firstPickedCard.getPartner() == secondPickedCard)
			changeState(State.NOTIFYING_CORRECT_MATCH);
		else
			changeState(State.NOTIFYING_INCORRECT_MATCH);
	}

	/**
	 * Clears the two currently chosen cards away if they are not a matched pair
	 */
	private void clearUnmatchedFaceUpCards()
	{
		if (firstPickedCard != null)
		{
			if (firstPickedCard.getState() != Card.State.MATCHED)
			{
				firstPickedCard.turnTofaceDown();
				firstPickedCard.setBackground(null);
			}
			firstPickedCard = null;
		}

		if (secondPickedCard != null)
		{
			if (secondPickedCard.getState() != Card.State.MATCHED)
			{
				secondPickedCard.setBackground(null);
				secondPickedCard.turnTofaceDown();
			}
			secondPickedCard = null;
		}
	}

	/**
	 * Picks a set of images which which to create pairs of Cards with, and then
	 * adds those Cards to the game.
	 */
	private void getNewCards()
	{
		// Remove any existing cards in the game and reset the game score
		cardPanel.removeAll();
		cards.clear();
		resetScore();

		// Initialise the variables used in the process
		imagesToBeUsed = new ImageIcon[numberOfPairs];
		indexesUsed = new ArrayList<Integer>();
		
		int index;
		String name, path;
		URL url;

		// Create a bunch of Image Icons for the Cards
		for (int i = 0; i < numberOfPairs; i++)
		{
			// Keep generating random indexes until one is found which has not
			// been used before
			do
				index = rand.nextInt(this.imageList.size());
			while (indexesUsed.contains(index));
			indexesUsed.add(index);

			// Get the name, path and URL of image
			name = this.imageList.get(index);
			path = "images/" + name;
			url = getClass().getClassLoader().getResource(path);

			// Attempt to read the image and create an ImageIcon for it
			try
			{
				imagesToBeUsed[i] = new ImageIcon(ImageIO.read(url), name.substring(0, name.length() - 4));
			}
			catch (IOException ex)
			{
				System.err.println("Couldn't read " + path);
			}
		}

		// Create the Cards using the ImageIcons just created
		for (int i = 0; i < numberOfPairs; i++)
		{
			// Create two Cards, both with the same ImageIcon
			Card card1 = new Card(imagesToBeUsed[i]);
			Card card2 = new Card(imagesToBeUsed[i]);

			// Tell each card about its partner
			card1.setPartner(card2);
			card2.setPartner(card1);

			// Specify the Game object as the the Cards ActionListener
			card1.addActionListener(this);
			card2.addActionListener(this);

			// Add the cards to the central list of Cards in the game
			cards.add(card1);
			cards.add(card2);
		}

		// Shuffle the the list of Cards such that they can be placed in the
		// game randomly
		Collections.shuffle(cards);

		// Now that the Cards are ordered randomly add them to the cardPanel
		for (Card card : cards)
			cardPanel.add(card);

		// Since the panel has changed we need to layout the panels components again
		cardPanel.validate();

		changeState(State.WAITING_FOR_FIRST_CARD);
	}

	/**
	 * Updates the game score based upon the type parameter. Also updates the
	 * JLabels which represent the games score, and ends round if all Cards
	 * have been matched
	 *
	 * @param type A MatchType enum of HIT or MISS
	 */
	private void updateScore(MatchType type)
	{
		switch (type)
		{
			case HIT:
				hits += 1;
				hitsLabel.setText("HITS: " + Integer.toString(hits));
				if (hits == numberOfPairs)
					changeState(State.NOTIFYING_COMPLETE);
				break;
			case MISS:
				misses += 1;
				missesLabel.setText("MISSES: " + Integer.toString(misses));
		}
	}

	/**
	 * Resets the Game score, invoked when the shuffle button is pressed
	 */
	private void resetScore()
	{
		hits = 0;
		hitsLabel.setText("HITS: 0");

		misses = 0;
		missesLabel.setText("MISSES: 0");
	}

	/**
	 * A utility method which checks that two string are equal after first
	 * checking that they are both not null
	 *
	 * @param str1 The first string to compare with the second
	 * @param str2 The second string to compare with the first
	 * @return True if both strings are not null and are equal
	 */
	private boolean areEqual(String str1, String str2)
	{
		return str1 != null && str2 != null && str1.equals(str2);
	}

	private boolean changeState(State newState)
	{
		boolean changed = newState != state;

		state = newState;

		switch (newState)
		{
			case NOTIFYING_COMPLETE:
//				for (Card card : cards)
//					card.setEnabled(true);
				// There is supposed to be no break here
			case SOLVED:
				for (Card card : cards)
				{
					if (card.getState() != Card.State.MATCHED)
						card.setBackground(Card.redColour);
					card.turnToFaceUp();
				}
				solve.setEnabled(false);
				shuffle.setText(SHUFFLE_RESTART_TEXT);
				break;
			case NOTIFYING_CORRECT_MATCH:
				firstPickedCard.hasBeenMatched();
				secondPickedCard.hasBeenMatched();

				clearUnmatchedFaceUpCards();
				updateScore(MatchType.HIT);
				if (state != State.NOTIFYING_COMPLETE)
					changeState(State.WAITING_FOR_FIRST_CARD);
				break;
			case NOTIFYING_INCORRECT_MATCH:
				firstPickedCard.setBackground(Card.redColour);
				secondPickedCard.setBackground(Card.redColour);
				updateScore(MatchType.MISS);
				// Start the timer in order to wait before flipping the mismatched
				// cards back over.
				incorrectMatchTimeout.restart();
				break;
			case WAITING_FOR_FIRST_CARD:
				shuffle.setText(SHUFFLE_NORMAL_TEXT);
				clearUnmatchedFaceUpCards();
				firstPickedCard = null;
				secondPickedCard = null;
				solve.setEnabled(true);
				break;
			case WAITING_FOR_SECOND_CARD:
		}

		return changed;
	}

	/**
	 * The main function which kicks the game off!
	 *
	 * @param args Not used
	 */
	public static void main(String[] args)
	{
		Game game = new Game();
	}
}
