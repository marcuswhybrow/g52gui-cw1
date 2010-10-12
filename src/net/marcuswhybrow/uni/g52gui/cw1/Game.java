package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author marcus
 */
public class Game extends JFrame implements ActionListener
{
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Card firstPickedCard = null;
	private Card secondPickedCard = null;
	
	private enum State {
		WAITING_FOR_FIRST_CARD,
		WAITING_FOR_SECOND_CARD,
		NOTIFYING_INCORRECT_MATCH,
		NOTIFYING_CORRECT_MATCH,
		NOTIFY_COMPLETE
	}
	private State state = State.WAITING_FOR_FIRST_CARD;

	public Game()
	{
		this.setLocation(500, 500);
		this.setSize(500, 600);
		this.setTitle("The Concentration Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(4,3));

		for (int i = 0; i < 6; i++)
		{
			String name = Integer.toString(i);
			Card card1 = new Card(name);
			Card card2 = new Card(name);
			
			// Tell each card about its partner
			card1.setPartner(card2);
			card2.setPartner(card1);

			this.add(card1);
			this.add(card2);

			card1.addActionListener(this);
			card2.addActionListener(this);
		}

		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		Card card = ((Card) e.getSource());
		switch (this.state)
		{
			case WAITING_FOR_FIRST_CARD:
				if (card.turnTofaceUp())
				{
					this.firstPickedCard = card;
					this.state = State.WAITING_FOR_SECOND_CARD;
				}
				break;

			case WAITING_FOR_SECOND_CARD:
				if (card.turnTofaceUp())
				{
					this.secondPickedCard = card;
					this.checkChosenCards();
				}
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
			this.clearLimboCards();
			this.state = State.WAITING_FOR_FIRST_CARD;
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

	public static void main(String[] args)
	{
		Game game = new Game();
	}
}
