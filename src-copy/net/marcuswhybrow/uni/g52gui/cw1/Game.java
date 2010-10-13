package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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

	private JPanel cardsPanel = new JPanel();
	private JPanel controlsPanel = new JPanel();

	JButton solve = new JButton("Solve");
	JButton shuffle = new JButton("Shuffle");

	private int numberOfPairs = 6;
	private int numberOfPairsMatched = 0;
	
	private enum State {
		WAITING_FOR_FIRST_CARD,
		WAITING_FOR_SECOND_CARD,
		NOTIFYING_INCORRECT_MATCH,
		NOTIFYING_CORRECT_MATCH,
		NOTIFY_COMPLETE
	}
	private State state = State.WAITING_FOR_FIRST_CARD;

	private File[] images;

	private Timer timer;

	private ArrayList<String> imageList = new ArrayList<String>();

	private String INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND = "incorrect_timeout";
	private String SHUFFLE_ACTION_COMMAND = "shuffle";
	private String SOLVE_ACTION_COMMAND = "solve";

	public Game()
	{
		timer = new Timer(1000, this);
		timer.setRepeats(false);
		timer.setActionCommand(INCORRECT_MATCH_TIMEOUT_ACTION_COMMAND);

		this.setLocation(500, 500);
		this.setSize(200, 300);
		this.setTitle("The Concentration Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.add(cardsPanel, BorderLayout.CENTER);
		this.add(controlsPanel, BorderLayout.SOUTH);

		this.cardsPanel.setLayout(new GridLayout(4, 3));

		this.controlsPanel.setLayout(new GridLayout(1, 2));

		this.solve.addActionListener(this);
		this.shuffle.addActionListener(this);

		this.solve.setActionCommand(SOLVE_ACTION_COMMAND);
		this.shuffle.setActionCommand(SHUFFLE_ACTION_COMMAND);

		this.controlsPanel.add(solve);
		this.controlsPanel.add(shuffle);

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
			this.state = State.WAITING_FOR_FIRST_CARD;
		}
		else
		{
			this.state = State.NOTIFYING_INCORRECT_MATCH;
			this.timer.start();
		}
	}

	private void clearLimboCards()
	{
		if (this.firstPickedCard != null)
		{
			if (! this.firstPickedCard.isMatched())
			{
				this.firstPickedCard.turnTofaceDown();
				this.numberOfPairsMatched += 1;
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
		this.cardsPanel.removeAll();

		ArrayList<Card> newCards = new ArrayList<Card>();
		Random rand = new Random();
		Image[] imagesToBeUsed = new Image[numberOfPairs];

		for (int i = 0; i < numberOfPairs; i++)
		{
			String path = "images/" + this.imageList.get(rand.nextInt(this.imageList.size()));
			URL url = getClass().getClassLoader().getResource(path);
			try
			{
				imagesToBeUsed[i] = ImageIO.read(url);
			}
			catch (IOException ex)
			{
				System.err.println("Couldn't read " + path);
			}
		}

		for (int i = 0; i < numberOfPairs; i++)
		{
			ImageIcon icon = new ImageIcon(imagesToBeUsed[i]);
			Card card1 = new Card(icon);
			Card card2 = new Card(icon);

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
			this.cardsPanel.add(card);
			this.cards.add(card);
		}

		this.cardsPanel.validate();
	}

	public static void main(String[] args)
	{
		Game game = new Game();
	}
}
