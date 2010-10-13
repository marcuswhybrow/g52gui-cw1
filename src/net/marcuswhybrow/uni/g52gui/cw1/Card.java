package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * This class represents a card which may be clicked with mouse revealing a
 * hidden graphic.
 *
 * A Card can be in several states FACEDOWN, FACEUP and MATCHED.
 *
 * All Cards in the game start in the FACEDOWN state such that their graphics
 * are hidden
 *
 * The player clicks on cards in order to turn them FACEUP, once two have been
 * chosen the Game determines if they match or not. If they match both cards
 * enter the MATCHED state, otherwise both cards return to the FACEDOWN state.
 *
 * @author Marcus Whybrow
 */
public class Card extends JButton
{
	/** The state a Card can be in */
	public enum State {FACEDOWN, FACEUP, MATCHED}
	/** The state this Card is currently in */
	private State state = State.FACEDOWN;
	/** The Card which is paired with this Card */
	private Card partner = null;
	/** The graphic this Card will display once in the FACEUP state */
	private ImageIcon icon;

	public static Color redColour;
	public static Color greenColour;
	public static Color grayColour;

	/**
	 * Accepts a single ImageIcon which sould represent the graphic revealed
	 * once the Card is clicked by the player.
	 *
	 * @param icon The graphic this card should display when FACEUP
	 */
	public Card(ImageIcon icon)
	{
		super();
		this.icon = icon;
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
		setOpaque(true);

		if (redColour == null)
		{
			float[] redColourHSB = new float[3];
			Color.RGBtoHSB(255, 128, 128, redColourHSB);
			redColour = Color.getHSBColor(redColourHSB[0], redColourHSB[1], redColourHSB[2]);
		}

		if (greenColour == null)
		{
			float[] greenColourHSB = new float[3];
			Color.RGBtoHSB(128, 255, 128, greenColourHSB);
			greenColour = Color.getHSBColor(greenColourHSB[0], greenColourHSB[1], greenColourHSB[2]);
		}

		if (grayColour == null)
		{
			float[] grayColourHSB = new float[3];
			Color.RGBtoHSB(240, 240, 240, grayColourHSB);
			grayColour = Color.getHSBColor(grayColourHSB[0], grayColourHSB[1], grayColourHSB[2]);
		}

		setBackground(grayColour);
	}

	/**
	 * Returns the current state of this Card
	 *
	 * @return An enum from Card.State
	 */
	public State getState()
	{
		return state;
	}

	/**
	 * Turn this card to face up revealing its graphic. If the card is FACEDOWN
	 * then its state is changed to FACEUP. If the card was already FACEUP then
	 * no change is made.
	 *
	 * @return True if the card state changed from FACEDOWN to FACEUP
	 */
	public boolean turnToFaceUp() {
		boolean turned = state == State.FACEDOWN;
		if (state == State.FACEDOWN)
		{
			state = State.FACEUP;
			setIcon(icon);
			setToolTipText(icon.getDescription());
			return true;
		}
		return false;
	}

	/**
	 *
	 * Turn this card to face down hiding its graphic. If the card is FACEUP
	 * then its state is changed to FACEDOWN. If the card was already FACEDOWN
	 * then no change is mage.
	 *
	 * @return True if the card state changed from FACEUP to FACEDOWN
	 */
	public boolean turnTofaceDown() {
		if (state == State.FACEUP)
		{
			state = State.FACEDOWN;
			setIcon(null);
			setToolTipText(null);
			return true;
		}
		return false;
	}

	/**
	 * Informs this Card of the Card with which it is paired, its so called
	 * partner Card which has a grahic which matches this Card's graphic.
	 *
	 * @param partner The Card with which this Card is paired
	 */
	public void setPartner(Card partner)
	{
		this.partner = partner;
	}

	/**
	 * Returns the Card which this Card is paried with. Used in order to
	 * compare the returned Card with another Card the player has chose, if they
	 * are the same then a match (or hit) has occurred.
	 *
	 * @return The Card which is paired with this Card
	 */
	public Card getPartner()
	{
		return partner;
	}

	/**
	 * Inform this Card that it should move into the MATCHED state, altering
	 * itself visually in order to advertise the fact that it is no longer
	 * available as a choice.
	 */
	public void hasBeenMatched()
	{
		state = State.MATCHED;
		setBackground(greenColour);
	}

	@Override
	public void setBackground(Color color)
	{
		if (color == null)
			super.setBackground(grayColour);
		else
			super.setBackground(color);
	}
}
