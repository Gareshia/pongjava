import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


public class Ball extends Sprite  {

	Sprite schl�ger;
	

	
	public Ball(double _x, double _y, double _h, double _w, BufferedImage _bild, 
												GamePanel _spielfeld, double _speed, Sprite _schl�ger)
	{
		super(_x, _y, _h, _w, _bild, _spielfeld);
		this.verticalSpeed = _speed;
		this.schl�ger = _schl�ger;
		this.setzeStartWerte();
		
	
		
	}
	
	private void setzeStartWerte()
	{
		this.setVerticalSpeed(verticalSpeed);
		
	}
	
	public void pralleAbVonSchl�ger()
	{
		double x = schl�ger.x;
		double y = schl�ger.y;
		double breite= schl�ger.getWidth();
		double h�he = schl�ger.getHeight();
		
		if (this.intersects(x, y, breite/5, h�he))
			{
				
				this.verticalSpeed = -SPEED;
				this.horizontalSpeed = -SPEED ;
			
			}
		
		else if(this.intersects(x + breite/5, y, 3*(breite/5), h�he))
		{
			
			this.horizontalSpeed = 0;
			this.verticalSpeed = -verticalSpeed;
		}
		
		else if(this.intersects(x + (breite- breite/5), y, breite/5, h�he))
		{
			
			this.verticalSpeed = -SPEED;
			this.horizontalSpeed = SPEED; 
		}
			
	}
	
	public void pralleAbVonWand(boolean horizontaleWand)
	{
		if(horizontaleWand) //abprall von einer horizontalen wand
			this.verticalSpeed = -this.verticalSpeed;
		
		else  //abprall von vertikaler wand
			this.horizontalSpeed = -this.horizontalSpeed;
	}
	
	public void pralleAbVonBlock(Sprite block)
	{
		if(this.intersectsLine(block.x, block.y, block.x + block.getWidth(), block.y)
				|| this.intersectsLine(block.x, block.y + block.getHeight(), block.x + block.getWidth(), block.y + block.getHeight()))
				this.verticalSpeed = -verticalSpeed;
		
		else if(this.intersectsLine(block.x, block.y, block.x, block.y + block.getHeight())
				|| this.intersectsLine(block.x + block.getWidth(), block.y, block.x + block.getWidth(), block.y + block.getWidth()))
			this.horizontalSpeed= -horizontalSpeed;
				
	}
	
	@Override
	public void bewegeDich(long delta)
	{
		this.x += horizontalSpeed * (delta/1e9); //die geschwindigkeit wird in Abh�ngigkeit zur fps gesetzt
		this.y += verticalSpeed * (delta/1e9); 
	}
	
	/**
	 * diese methode setzt den Ball in die mitte des spielfelds
	 */
	public void resetPosition()
	{
		this.x = spielfeld.getWidth()/2;
		this.y = spielfeld.getHeight()/2;
	}
	
	public void resetMovement()
	{
		horizontalSpeed = 0;
	}
	
	
	
	
	
}
