import java.awt.Graphics;
import java.awt.image.BufferedImage;


public class Block extends Sprite{

	BufferedImage[] animationsBilder;
	long animationCounter = 0;
	long animationDelay = 30;
	int currPicIndex = 0;
	
	
	private boolean hit = false;
	public boolean animationFinished = false;
	
	public Block(double _x, double _y, double _h, double _w,
						BufferedImage _bild, GamePanel _spielfeld, 
						BufferedImage[] _animationsBilder)
	{
		super(_x, _y, _h, _w, _bild, _spielfeld);
		this.animationsBilder = _animationsBilder;
		
	}
	
	
	
	public void animierDich(Graphics g, long delta)
	{	
		if(currPicIndex < animationsBilder.length)
		{	
			
			g.drawImage(animationsBilder[currPicIndex], (int)this.getX(), (int)this.getY(), null);
		
			animationCounter += delta / 1000000; //undwandlung von nano- in millisekdunden		
			
			if(animationCounter > animationDelay)
			{
				currPicIndex ++;
				animationCounter = 0;
			}
				
		}
		else
			this.animationFinished = true;
		
	}
	
	public void setHit()
	{
		this.hit = true;
	}
	
	public boolean isHit()
	{
		return this.hit;
	}
	
}

