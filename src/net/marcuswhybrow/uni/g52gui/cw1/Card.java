package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.Color;
import javax.swing.JButton;

/**
 *
 * @author Marcus Whybrow
 */
public class Card extends JButton
{
	public enum State {FACEDOWN, FACEUP, MATCHED}

	private String name = null;
	private State state = State.FACEDOWN;
	private Card partner = null;

	public Card(String name)
	{
		this.name = name;
	}

	public boolean turnTofaceUp() {
		boolean turned = this.state == State.FACEDOWN;
		this.state = State.FACEUP;
		this.setText(name);
		return turned;
	}

	public boolean turnTofaceDown() {
		boolean turned = this.state == State.FACEUP;
		this.state = State.FACEDOWN;
		this.setText("");
		return turned;
	}

	public void setPartner(Card partner)
	{
		this.partner = partner;
	}

	public Card getPartner()
	{
		return this.partner;
	}

	public void hasBeenMatched()
	{
		this.state = State.MATCHED;
		this.setForeground(Color.red);
	}

	public boolean isMatched()
	{
		return this.state == State.MATCHED;
	}
}
