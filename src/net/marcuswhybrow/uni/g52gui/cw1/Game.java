package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
 *
 * @author marcus
 */
public class Game extends JFrame implements ActionListener
{
	private ArrayList<Card> cards = new ArrayList<Card>();

	private Card firstPickedCard = null;
	private Card secondPickedCard = null;

	private JPanel cardPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();

	private JPanel controlPanel = new JPanel();
	private JPanel scorePanel = new JPanel();

	JButton solve = new JButton("Solve");
	JButton shuffle = new JButton("Shuffle");

	JLabel hitsLabel = new JLabel("HITS: 0");
	JLabel missesLabel = new JLabel("MISSES: 0");

	private int numberOfPairs = 20;

	private enum MatchType {HIT, MISS}
	
	private enum State {
		WAITING_FOR_FIRST_CARD,
		WAITING_FOR_SECOND_CARD,
		NOTIFYING_INCORRECT_MATCH,
		NOTIFYING_CORRECT_MATCH,
		NOTIFY_COMPLETE
	}
	private State state = State.WAITING_FOR_FIRST_CARD;

	private Timer incorrectMatchTimeout;

	private ArrayList<String> imageList = new ArrayList<String>();

	private String INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND = "incorrect_timeout";
	private String SHUFFLE_ACTION_COMMAND = "shuffle";
	private String SOLVE_ACTION_COMMAND = "solve";

	/** The number of times in the round a match was detected */
	private int hits = 0;
	/** The number of times in the round a miss was detected */
	private int misses = 0;

	public Game()
	{
		incorrectMatchTimeout = new Timer(1000, this);
		incorrectMatchTimeout.setRepeats(false);
		incorrectMatchTimeout.setActionCommand(INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND);

		this.setLocation(500, 500);
		this.setSize((numberOfPairs/4) * 140, 300);
		this.setTitle("The Concentration Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.add(cardPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

		this.cardPanel.setLayout(new GridLayout(4, numberOfPairs/4));

		this.bottomPanel.setLayout(new BorderLayout());
		this.bottomPanel.add(this.controlPanel, BorderLayout.WEST);
		this.bottomPanel.add(this.scorePanel, BorderLayout.EAST);

		this.controlPanel.setLayout(new GridLayout(1, 2));
		this.scorePanel.setLayout(new GridLayout(1, 2));

		this.scorePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 30));
		this.controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));

		this.solve.addActionListener(this);
		this.shuffle.addActionListener(this);

		this.solve.setActionCommand(SOLVE_ACTION_COMMAND);
		this.shuffle.setActionCommand(SHUFFLE_ACTION_COMMAND);

		this.controlPanel.add(solve);
		this.controlPanel.add(shuffle);

		this.scorePanel.add(hitsLabel);
		this.scorePanel.add(missesLabel);

		String line;
		InputStream is = getClass().getClassLoader().getResourceAsStream("images/list.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try
		{
			while (null != (line = br.readLine()))
				imageList.add(line);
		}
		catch (IOException ex)
		{
			System.out.println("Couldn't read the list of images");
		}

		this.getNewCards();

		this.setVisible(true);
	}

	private ImageIcon createImageIcon(String path, String description)
	{
		URL url = getClass().getClassLoader().getResource(path);
		// sprite = ImageIO.read(url);
		if (url != null)
		{
			return new ImageIcon(url, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (e.getActionCommand() != null && e.getActionCommand().equals(SOLVE_ACTION_COMMAND))
		{
			for (Card card : cards)
				{
					card.turnToFaceUp();
				}
		}
		else if (e.getActionCommand() != null && e.getActionCommand().equals(SHUFFLE_ACTION_COMMAND))
		{
			this.getNewCards();
		}
		else if (source instanceof Card)
		{
			Card card = (Card) source;
			switch (this.state)
			{
				case WAITING_FOR_FIRST_CARD:
					if (card.turnToFaceUp())
					{
						this.firstPickedCard = card;
						this.state = State.WAITING_FOR_SECOND_CARD;
					}
					break;

				case WAITING_FOR_SECOND_CARD:
					if (card.turnToFaceUp())
					{
						this.secondPickedCard = card;
						this.checkChosenCards();
					}
					break;
				case NOTIFYING_INCORRECT_MATCH:
					if (card.turnToFaceUp())
					{
						this.incorrectMatchTimeout.stop();
						this.clearLimboCards();
						this.firstPickedCard = card;
						this.state = State.WAITING_FOR_SECOND_CARD;
					}
			}
		}
		else if (e.getActionCommand() != null && e.getActionCommand().equals(INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND) && this.state == State.NOTIFYING_INCORRECT_MATCH)
		{
			this.clearLimboCards();
			this.state = State.WAITING_FOR_FIRST_CARD;
		}
	}

	private void checkChosenCards()
	{
		if (firstPickedCard.getPartner() == secondPickedCard)
		{
			this.state = State.NOTIFYING_CORRECT_MATCH;

			firstPickedCard.hasBeenMatched();
			secondPickedCard.hasBeenMatched();
			this.clearLimboCards();

			this.updateScore(MatchType.HIT);
			this.state = State.WAITING_FOR_FIRST_CARD;
		}
		else
		{
			this.state = State.NOTIFYING_INCORRECT_MATCH;
			this.updateScore(MatchType.MISS);

			// Start the timer in order to wait before flipping the mismatched
			// cards back over.
			this.incorrectMatchTimeout.restart();
		}
	}

	private void clearLimboCards()
	{
		if (this.firstPickedCard != null)
		{
			if (! this.firstPickedCard.isMatched())
			{
				this.firstPickedCard.turnTofaceDown();
			}
			this.firstPickedCard = null;
		}

		if (this.secondPickedCard != null)
		{
			if (! this.secondPickedCard.isMatched())
			{
				this.secondPickedCard.turnTofaceDown();
			}
			this.secondPickedCard = null;
		}
	}

	private void getNewCards()
	{
		this.cardPanel.removeAll();

		this.resetScore();

		ArrayList<Card> newCards = new ArrayList<Card>();
		Random rand = new Random();
		ImageIcon[] imagesToBeUsed = new ImageIcon[numberOfPairs];
		ArrayList<Integer> indexesUsed = new ArrayList<Integer>();
		int index;

		for (int i = 0; i < numberOfPairs; i++)
		{
			String path;
			do
			{
				index = rand.nextInt(this.imageList.size());
			}
			while (indexesUsed.contains(index));
			indexesUsed.add(index);

			String name = this.imageList.get(index);
			path = "images/" + name;
			URL url = getClass().getClassLoader().getResource(path);
			try
			{
				imagesToBeUsed[i] = new ImageIcon(ImageIO.read(url), name.substring(0, name.length() - 4));
			}
			catch (IOException ex)
			{
				System.err.println("Couldn't read " + path);
			}
		}

		for (int i = 0; i < numberOfPairs; i++)
		{

			Card card1 = new Card(imagesToBeUsed[i]);
			Card card2 = new Card(imagesToBeUsed[i]);

			// Tell each card about its partner
			card1.setPartner(card2);
			card2.setPartner(card1);

			card1.addActionListener(this);
			card2.addActionListener(this);

			newCards.add(card1);
			newCards.add(card2);
		}

		Collections.shuffle(newCards);

		for (Card card : newCards)
		{
			this.cardPanel.add(card);
			this.cards.add(card);
		}

		this.cardPanel.validate();
	}

	private void updateScore(MatchType type)
	{
		switch (type)
		{
			case HIT:
				this.hits += 1;
				this.hitsLabel.setText("HITS: " + Integer.toString(this.hits));
				if (this.hits == this.numberOfPairs)
				{
					for (Card card : this.cards)
					{
						card.setBackground(Color.GREEN);
					}
				}
				break;
			case MISS:
				this.misses += 1;
				this.missesLabel.setText("MISSES: " + Integer.toString(this.misses));
		}
	}

	private void resetScore()
	{
		this.hits = 0;
		this.hitsLabel.setText("HITS: 0");

		this.misses = 0;
		this.missesLabel.setText("MISSES: 0");
	}

	public static void main(String[] args)
	{
		Game game = new Game();
	}
}
