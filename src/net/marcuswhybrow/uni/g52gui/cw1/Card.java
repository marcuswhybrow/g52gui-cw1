package net.marcuswhybrow.uni.g52gui.cw1;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 * @author Marcus Whybrow
 */
public class Card extends JButton
{
	public enum State {FACEDOWN, FACEUP, MATCHED}

	private State state = State.FACEDOWN;
	private Card partner = null;
	private ImageIcon icon;

	public Card(ImageIcon icon)
	{
		super();
		this.icon = icon;
	}

	public String getState()
	{
		switch (this.state)
		{
			case FACEDOWN:
				return "FACEDOWN";
			case FACEUP:
				return "FACEUP";
			case MATCHED:
				return "MATCHED";
			default:
				return "";
		}
	}

	public boolean turnToFaceUp() {
		boolean turned = this.state == State.FACEDOWN;
		this.state = State.FACEUP;
		this.setIcon(this.icon);
		this.setToolTipText(icon.getDescription());
		return turned;
	}

	public boolean turnTofaceDown() {
		boolean turned = this.state == State.FACEUP;
		this.state = State.FACEDOWN;
		this.setIcon(null);
		this.setToolTipText(null);
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
		this.setOpaque(true);
		this.setBackground(Color.RED);
	}

	public boolean isMatched()
	{
		return this.state == State.MATCHED;
	}
}
