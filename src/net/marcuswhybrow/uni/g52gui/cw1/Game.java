package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.BorderLayout;
import java.awt.Color;
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
import net.marcuswhybrow.uni.g52gui.cw1.model.GameModel;
import net.marcuswhybrow.uni.g52gui.cw1.model.ModelListener;

/**
 * The Game class controls the mechanisms of the game and is also the class
 * which responds to Swing events such as mouse clicks etc.
 *
 * @author Marcus Whybrow
 */
public class Game extends JFrame implements ActionListener, ModelListener
{
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

	/** A game controlling button which starts the round again with new cards */
	private JButton shuffle = new JButton(GameModel.SHUFFLE_NORMAL_TEXT);

	/** A label which displays the current number of matches (or hits) */
	private JLabel hitsLabel = new JLabel("HITS: 0");
	/** A label which displays the current number of misses */
	private JLabel missesLabel = new JLabel("MISSES: 0");

	/**
	 * A swing timer which fires an event a fixed time after an icorrect match
	 * in order to allow a period of leaving the cards face up, but eventually
	 * turning them back to be face down
	 */
	private Timer incorrectMatchTimeout;

	private GameModel gameModel = GameModel.get();

	Random rand = new Random();

	/** A list of relative paths to all the images which may be used as graphics
	 * for the cards
	 */
	ArrayList<String> imageList = new ArrayList<String>();

	/**
	 * The constructor of the game sets up all the initial values for variables,
	 * and lays out the components which represent the game.
	 */
	public Game()
	{
		gameModel.addModelListener(this);

		// Set the tile of the window and the size to roughly accomodate the
		// number of cards in use. Then place the window in the center of the
		// screen and tell the program to exit when the close button on this
		// window is clicked.
		setTitle(gameModel.getTitle());
		setMinimumSize(gameModel.getMinimumSize());
		setSize((gameModel.getNumberOfPairs()/4) * 140, 300);
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
		cardPanel.setLayout(new GridLayout(4, gameModel.getNumberOfPairs()/4));
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
		solve.setActionCommand(GameModel.SOLVE_ACTION_COMMAND);
		solve.addActionListener(this);

		// Next add the shuffle button to the controlPanel, set its ActionCommand
		// and specify that the Game class is the listener
		controlPanel.add(shuffle);
		shuffle.setActionCommand(GameModel.SHUFFLE_ACTION_COMMAND);
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
		incorrectMatchTimeout = new Timer(GameModel.MISS_TIMEOUT_PERIOD, this);
		incorrectMatchTimeout.setRepeats(false);
		incorrectMatchTimeout.setActionCommand(GameModel.INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND);

		// Get the list of possible images which can be attributed to a Card
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(GameModel.IMAGE_LIST_FILE)));
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
		if (areEqual(e.getActionCommand(), GameModel.SOLVE_ACTION_COMMAND))
			changeState(GameModel.State.SOLVED);

		// The shuffle button has been pressed
		else if (areEqual(e.getActionCommand(), GameModel.SHUFFLE_ACTION_COMMAND))
			getNewCards();

		// The non-matched cards timeout has occurred
		else if (areEqual(e.getActionCommand(), GameModel.INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND) && gameModel.getState() == GameModel.State.NOTIFYING_INCORRECT_MATCH)
			changeState(GameModel.State.WAITING_FOR_FIRST_CARD);

		// A Card has been chosen
		else if (source instanceof Card)
		{
			Card card = (Card) source;
			switch (gameModel.getState())
			{
				case WAITING_FOR_FIRST_CARD:
					if (card.turnToFaceUp())
					{
						gameModel.setFirstPickedCard(card);
						changeState(GameModel.State.WAITING_FOR_SECOND_CARD);
					}
					break;

				case WAITING_FOR_SECOND_CARD:
					if (card.turnToFaceUp())
					{
						gameModel.setSecondPickedCard(card);
						checkChosenCards();
					}
					break;
				case NOTIFYING_INCORRECT_MATCH:
					if (card.turnToFaceUp())
					{
						incorrectMatchTimeout.stop();
						clearUnmatchedFaceUpCards();
						gameModel.setFirstPickedCard(card);
						changeState(GameModel.State.WAITING_FOR_SECOND_CARD);
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
		if (gameModel.getFirstPickedCard().getPartner() == gameModel.getSecondPickedCard())
			changeState(GameModel.State.NOTIFYING_CORRECT_MATCH);
		else
			changeState(GameModel.State.NOTIFYING_INCORRECT_MATCH);
	}

	/**
	 * Clears the two currently chosen cards away if they are not a matched pair
	 */
	private void clearUnmatchedFaceUpCards()
	{
		if (gameModel.getFirstPickedCard() != null)
		{
			if (gameModel.getFirstPickedCard().getState() != Card.State.MATCHED)
			{
				gameModel.getFirstPickedCard().turnTofaceDown();
				gameModel.getFirstPickedCard().setBackground(null);
				gameModel.modelHasChanged();
			}
			gameModel.setFirstPickedCard(null);
		}

		if (gameModel.getSecondPickedCard() != null)
		{
			if (gameModel.getSecondPickedCard().getState() != Card.State.MATCHED)
			{
				gameModel.getSecondPickedCard().setBackground(null);
				gameModel.getSecondPickedCard().turnTofaceDown();
				gameModel.modelHasChanged();
			}
			gameModel.setSecondPickedCard(null);
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
		
		gameModel.getCards().clear();
		gameModel.modelHasChanged();

		resetScore();

		// Initialise the variables used in the process
		ImageIcon[] imagesToBeUsed = new ImageIcon[gameModel.getNumberOfPairs()];
		ArrayList<Integer> indexesUsed = new ArrayList<Integer>();
		
		int index;
		String name, path;
		URL url;

		// Create a bunch of Image Icons for the Cards
		for (int i = 0; i < gameModel.getNumberOfPairs(); i++)
		{
			// Keep generating random indexes until one is found which has not
			// been used before
			do
				index = rand.nextInt(imageList.size());
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
		for (int i = 0; i < gameModel.getNumberOfPairs(); i++)
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
			gameModel.getCards().add(card1);
			gameModel.getCards().add(card2);
			gameModel.modelHasChanged();
		}

		// Shuffle the the list of Cards such that they can be placed in the
		// game randomly
		Collections.shuffle(gameModel.getCards());

		// Now that the Cards are ordered randomly add them to the cardPanel
		for (Card card : gameModel.getCards())
			cardPanel.add(card);

		// Since the panel has changed we need to layout the panels components again
		cardPanel.validate();

		changeState(GameModel.State.WAITING_FOR_FIRST_CARD);
	}

	/**
	 * Updates the game score based upon the type parameter. Also updates the
	 * JLabels which represent the games score, and ends round if all Cards
	 * have been matched
	 *
	 * @param type A MatchType enum of HIT or MISS
	 */
	private void updateScore(GameModel.MatchType type)
	{
		switch (type)
		{
			case HIT:
				gameModel.setHits(gameModel.getHits() + 1);
				hitsLabel.setText("HITS: " + Integer.toString(gameModel.getHits()));
				if (gameModel.getHits() == gameModel.getNumberOfPairs())
					changeState(GameModel.State.NOTIFYING_COMPLETE);
				break;
			case MISS:
				gameModel.setMisses(gameModel.getMisses() + 1);
				missesLabel.setText("MISSES: " + Integer.toString(gameModel.getMisses()));
		}
	}

	/**
	 * Resets the Game score, invoked when the shuffle button is pressed
	 */
	private void resetScore()
	{
		gameModel.setHits(0);
		hitsLabel.setText("HITS: 0");

		gameModel.setMisses(0);
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

	private boolean changeState(GameModel.State newState)
	{
		boolean changed = newState != gameModel.getState();

		gameModel.setState(newState);

		switch (newState)
		{
			case NOTIFYING_COMPLETE:
//				for (Card card : cards)
//					card.setEnabled(true);
				// There is supposed to be no break here
			case SOLVED:
				for (Card card : gameModel.getCards())
				{
					if (card.getState() != Card.State.MATCHED)
						card.setBackground(Card.redColour);
					card.turnToFaceUp();
				}
				solve.setEnabled(false);
				shuffle.setText(GameModel.SHUFFLE_RESTART_TEXT);
				break;
			case NOTIFYING_CORRECT_MATCH:
				gameModel.getFirstPickedCard().hasBeenMatched();
				gameModel.getSecondPickedCard().hasBeenMatched();

				clearUnmatchedFaceUpCards();
				updateScore(GameModel.MatchType.HIT);
				if (gameModel.getState() != GameModel.State.NOTIFYING_COMPLETE)
					changeState(GameModel.State.WAITING_FOR_FIRST_CARD);
				break;
			case NOTIFYING_INCORRECT_MATCH:
				gameModel.getFirstPickedCard().setBackground(Card.redColour);
				gameModel.getSecondPickedCard().setBackground(Card.redColour);
				updateScore(GameModel.MatchType.MISS);
				// Start the timer in order to wait before flipping the mismatched
				// cards back over.
				incorrectMatchTimeout.restart();
				break;
			case WAITING_FOR_FIRST_CARD:
				shuffle.setText(GameModel.SHUFFLE_NORMAL_TEXT);
				clearUnmatchedFaceUpCards();
				gameModel.setFirstPickedCard(null);
				gameModel.setSecondPickedCard(null);
				solve.setEnabled(true);
				break;
			case WAITING_FOR_SECOND_CARD:
		}

		gameModel.modelHasChanged();

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

	private int count = 0;

	public void modelHasChanged()
	{
		System.out.println(count++);
	}
}
