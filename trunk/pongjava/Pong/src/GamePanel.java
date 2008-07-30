import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GamePanel  extends JPanel implements Runnable {
	
	Vector<Sprite> spieler;  //der Vektor, in dem die Sprites der Schläger abgelegt werden
	Vector<Sprite> blöcke;  //Verktor für die Blöcke
	Vector<Ball> bälle;  //Verktor für die Blöcke
	Vector<Sprite> lebenList;
	
	
	Vector<Sprite> löschen_blocks;
	Vector<Sprite> löschen_leben;
	
	int punkte = 0;
	Keylistener keylistener = new Keylistener(); //der Keylistener wird instanziiert
	final double SPIELERSPEED = 500; //die Bewegungsgeschwindigkeit des Schlägers
	boolean spielFortsetzen = true; //bool- variable für die Gameloop
	static Thread t; //der Thread in dem das Spiel läuft
	BufferedImage spielerBild; //das Bilderarray für die Spielerbilder
	BufferedImage ballBild;
	BufferedImage blockBild;
	BufferedImage lebenBild;
	BufferedImage pauseBild;
	BufferedImage gameOverBild;
	
	private boolean gameOver = false;
	private boolean pauseGame = false; //die Variable zum steuern des Pausen-modus
	Sprite pause;
	Sprite gameOverPic;
	final String BILDORDNER = "graphics\\";  //der Pfad des Bilderordners
	final String SCHLÄGER_BILDNAME = "schlaegerKlein.png";
	final String BALL_BILDNAME = "ball.png";
	final String BLOCK_BILDNAME = "block.png";
	final String LEBEN_BILDNAME = "leben.png";
	final String PAUSE_BILDNAME = "pause.png";
	final String GAMEOVER_BILDNAME = "gameOver.png";
	static boolean richtungVor = true; //hilfsvariable für das zusammensetzen des Bilderstrings
	static int bilderIndex = 0;
	
	private int leben = 3;
	
	long delta = 0;
	long last = 0;  //variablen für die errechnung der Framerate.
	long fps = 0;
	
	
	/**
	 * der Konstruktor erzeugt ein Fenster in der Größe der Übergebenen Variablen!
	 * @param x Breite des Fensters
	 * @param y Höhe des Fensters
	 */
	public GamePanel(int x, int y)
	{
		this.setPreferredSize(new Dimension(x, y));
		JFrame f = new JFrame("Pong!");
		f.addKeyListener(keylistener);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocation(100, 100);
		f.add(this);
		f.pack();
		f.setVisible(true);
		f.setResizable(false);
		this.inits();
	}
	
	/**
	 * diese Methode zeichnet das Fenster neu. 
	 * Sie kann mit repaint() angestoßen werden!
	 * repaint() befindet sich in der gameLoop
	 * 
	 * Die methode ruft paintComponent() der superklasse auf,
	 * dann wird das Graphics objet in ein Graphics2D objekt gecastet
	 * Daraufhin wird die methode zeichneObjekte() aufgerufen und das graphics objekt übergeben.
	 * Mit dieser Methode werden dann alle Block- objekte auf dem Bildschirm gezeichnet 
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		this.deleteBlocks();
		this.deleteLives();
		this.zeichneObjekte(g);
		
		g.setColor(Color.red);
		g.drawString(Long.toString(fps), 10,10);
		g.drawString(Integer.toString(punkte), 50, 10);
		
	}//end paintComponent
	
	/**
	 * diese Methode berechnet bei Aufruf die dauer des letzten durchlaufs und speichert diesen
	 * Wert in delta ab. die IV last wird dannach auf die aktuelle Systemzeit gesetzt.
	 * Die fps ergeben sich demnach aus der division der Nanosekunden-zahl für eine Sekunde mit 
	 * der Dauer des letzten durchlaufs.
	 */
	public void berechneFPS()
	{
		this.delta = System.nanoTime() - this.last;
		this.last = System.nanoTime();
		this.fps = (long)(1e9) / delta;
	}
	
	
	
	
	/**
	 * diese Methode wird im Konstruktor aufgerufen und initialisiert alle nötigen Variablen und
	 * Objekte
	 * Momentan ruft diese Methode die ladeBilder() methode für die spieler-Animationsbilder auf
	 * Mit dieser Animationsreihe wird daraufhin eine Instanz eines Blocks erstellt und dem Spieler-
	 * vektor hinzugefügt.
	 * Außerdem wird last auf die aktuelle Systemzei zum späteren errechnen der fps gesetzt.
	 */
	public void inits()
	{
		this.blöcke = new Vector<Sprite>();
		this.bälle = new Vector<Ball>();
		this.spieler = new Vector<Sprite>();
		this.löschen_blocks = new Vector<Sprite>();
		this.löschen_leben = new Vector<Sprite>();
		this.lebenList = new Vector<Sprite>();
		this.punkte = 0;
		this.last = System.nanoTime();
		this.spielerBild = ladeBild(SCHLÄGER_BILDNAME);
		this.ballBild = ladeBild(BALL_BILDNAME);
		this.blockBild = ladeBild(BLOCK_BILDNAME);
		this.lebenBild = ladeBild(LEBEN_BILDNAME);
		this.pauseBild = ladeBild(PAUSE_BILDNAME);
		this.gameOverBild = ladeBild(GAMEOVER_BILDNAME);
		
		gameOverPic = new Sprite(this.getWidth()/2 - gameOverBild.getWidth()/2, this.getHeight()/2- gameOverBild.getHeight()/2, gameOverBild.getHeight(), gameOverBild.getWidth(),
				gameOverBild, this);

		
		pause = new Sprite(this.getWidth()/2 - pauseBild.getWidth()/2, this.getHeight()/2- pauseBild.getHeight()/2, pauseBild.getHeight(), pauseBild.getWidth(),
														pauseBild, this);
		
		Sprite leben1 = new Sprite(750, 20, lebenBild.getHeight(), lebenBild.getWidth(), lebenBild, this);
		Sprite leben2 = new Sprite(750 + lebenBild.getWidth() + 1, 20, lebenBild.getHeight(), lebenBild.getWidth(), lebenBild, this);
		Sprite leben3 = new Sprite(750 + 2*lebenBild.getWidth() + 1, 20, lebenBild.getHeight(), lebenBild.getWidth(), lebenBild, this);
		
		Sprite spieler1 = new Sprite(this.getWidth()/2 - spielerBild.getWidth()/2 , this.getHeight()- spielerBild.getHeight() - 20,
				spielerBild.getHeight(), spielerBild.getWidth(), this.spielerBild, this);
		
		Ball ball1 = new Ball(this.getWidth()/2, this.getHeight()- 50, ballBild.getHeight(), 
						ballBild.getWidth(), this.ballBild, this, 200D, spieler1);
		
		Sprite block1 = new Sprite(this.getWidth()/2 - blockBild.getWidth()/2, 20, blockBild.getHeight(), blockBild.getWidth(), this.blockBild, this);
		
		blöcke.add(block1);
		spieler.add(spieler1);
		bälle.add(ball1);
		
		lebenList.add(leben1);
		lebenList.add(leben2);
		lebenList.add(leben3);
		
		this.resetBall();
		
		this.gameOver = false;
		this.pauseGame = false;
		this.leben = 3;
		
	}
	
	/**
	 * hier wird das spiel Pausiert, indem pauseGame "true" gesetzt wird.
	 */
	private void pauseGame()
	{
		this.pauseGame = true;
		keylistener.setSpace(true);
	}
	
	
	/**
	 * diese Methode setzt das spiel fort. Dabei werden auch die variablen zur 
	 * Errechnung der Framerate wieder auf 0 gesetzt.
	 */
	private void resumeGame()
	{
		
		this.last = System.nanoTime();
		
		
		this.pauseGame = false;
		keylistener.setSpace(false);
	}
	
	
	
	/**
	 * Diese Methode prüft auf gedrückte Pfeiltasten und ruft dann die methode der spieler-block sinstanzen
	 * zur erhöhung oder verringerung der bewegungsgeschwindigkeit.
	 * Die Geschwindigkeit SPIELERSPEED ist eine Konstante des GamePanels.
	 */
	public void checkKeys()
	{
		if(this.pauseGame == false)
		{
			if(this.keylistener.isLeft())
				for(Sprite s : spieler){
					s.setHorizontalSpeed(-SPIELERSPEED);
				}
					
			if(this.keylistener.isRight())
				for(Sprite s : spieler){
					s.setHorizontalSpeed(SPIELERSPEED);
				}
					
			if(!this.keylistener.isRight() && !this.keylistener.isLeft() )
				for(Sprite s : spieler){
					s.setHorizontalSpeed(0);
				}
			
			if(!this.keylistener.isDown()  &&  !this.keylistener.isUp())
				for(Sprite s : spieler){
					s.setVerticalSpeed(0);
				}
			this.pauseGame = keylistener.isSpace();
			
		}
		else
		{
			if(gameOver == false)
			{
				if(keylistener.isSpace() == false)
				{
					resumeGame();
				}
			}
			else
				if(keylistener.isSpace() == false)
				{
					startNewGame();
				}
				
					
			
		}
	}
	
	/**
	 * diese Methode ruft momentan für jedes Objekt im Spieler- vektor die BewegeDich() methode-
	 * auf, mit der die Objekte im Vektor abhängig von der bewegungsgeschwindigkeit bewegt werden!  
	 */
	public void bewegeObjekte(long delta)
	{
			
		for(Sprite s : spieler)
			s.bewegeDich(delta);
		
		for(Ball b : bälle)
			b.bewegeDich(delta);
		
			
	}
	
	
	/**
	 * in der run() -methode werden die Befehle in parallel laufenden Threads ausgeführt
	 * hier ist die game-loop untergebracht
	 * momentan werden hier die methoden checkKeys(), bewegeObjekte() und repaint()
	 * aufgerufen
	 * 
	 */
	public void run()
	{
		while(spielFortsetzen)
		{
			this.checkKeys();	//die tastatur wird auf gedrückte tasten abgefragt
			this.repaint(); //stößt drawComponents() an!
			if(pauseGame == false)
			{
				this.berechneFPS(); //berechnung der aktuellen fps
				
				
				this.checkIntersections();
				
				
				this.bewegeObjekte(delta); //die Objekte werden bewegt
				
				
				this.deleteBlocks();
				this.deleteLives();
				
				
				try{
					t.sleep(10);
				}catch(InterruptedException e) {}
			}	
		}
			
	}
	
	/**
	 * Diese Methode lädt bilder aus dem Bilderordner (Konstante des Spielfeldes) in ein 
	 * BufferedImage- array.
	 * Sowohl das BufferedImage- array, als auch der dateiname des Bildes und die anzahl der Einzelbilder
	 * wird der Funktion übergeben.
	 * Dabei wird  die anzahl der einzelbilder mal 2 genommen, damit die Animtion auch rückwärs
	 * abgespielt werden kann.
	 * @param _bilderArray das bilder- array, in das Bilder geladen werden sollen
	 * @param _anzahlBilder die anzahl der einzelbilder
	 * @param _dateiname der Name der Datei mit endung.
	 */
	public BufferedImage ladeBild(String bildName)
	{
		BufferedImage returnImage = null;
		
		URL bildURL;
			
			
			bildURL = getClass().getClassLoader().getResource(BILDORDNER + bildName);
			
			try{
				returnImage = ImageIO.read(bildURL);
				} catch(Exception e) {
					
					String pfad = "C:\\Users\\Dominik\\Desktop\\log.txt";
					try{
						FileWriter logWriter = new FileWriter(pfad);
						logWriter.append(bildName + " nicht gefunden!");
						logWriter.flush();
						logWriter.close();
					}catch(IOException k){}
					
					
					
				}
		
		return returnImage;
			
	}//end method
	
	
	
	
	/**
	 * Diese Methode prüft, ob der Ball an objekte anstößt und ruft dann die entsprechenden
	 * Methoden des Balls oder/und des angestoßenen Objekts auf. Die Methode testet
	 * auf kollisionen mit dem Schläger, den blöcken, aber auch den Bildschirmrändern.
	 */
	private void checkIntersections()
	{
		for(Ball b: bälle)
		{
			for(Sprite s: spieler)
			{
				if(b.intersects(s))
					b.pralleAbVonSchläger();
			}

			Iterator<Sprite> blöckeIter = blöcke.iterator();
			while(blöckeIter.hasNext())
			{
				Sprite curr = blöckeIter.next();
				if(b.intersects(curr))
				{
					
					b.pralleAbVonBlock(b);
					//blöckeIter.remove();  //löschen des blocks aus dem vektor
					löschen_blocks.add(curr);
					this.punkte += 10;
					break;	//hier wird aus der for- schleife gesprungen, um fehler zu vermeiden, da das aktuell durchlaufene element gelöscht wurde
					
				}
			}
			
			if(b.intersectsLine(0, 0, this.getWidth(), 0)) //oberer Bildschirmrand
			{
				b.pralleAbVonWand(true);
			}
			
			else if(b.intersectsLine(0, 0, 0, this.getHeight()))
			{


				b.pralleAbVonWand(false);
			}
			
			else if(b.intersectsLine(this.getWidth(), 0, this.getWidth(), this.getHeight()))
			{
				b.pralleAbVonWand(false);
			}
			
			else if(b.intersects(0, this.getHeight(), this.getWidth(), this.getHeight()))
			{
				reduceLives();
				
			}
		}
	}
	
	/**
	 * mit dieser Methode wird der Ball wieder in die mitte des Spielfeldes gesetzt.
	 */
	private void resetBall()
	{
		for(Ball i: bälle)
		{
			i.resetPosition();
			i.resetMovement();
		}
			
	}
	
	
	/**
	 * in dieser Methode werden die Leben des spielers um eins reduziert.
	 * außerdem wird der Ball in die Mitte des Spielfeldes gesetzt und das spiel pausiert. 
	 */
	private void reduceLives()
	{
		pauseGame();
		
		
		leben--;
		if(leben > -1)
		{
			Sprite deleteLife = lebenList.lastElement();
			löschen_leben.add(deleteLife);
			resetBall();
		}
		
		else
		{
			gameOver();
		}
			
	}
	
	/**
	 * mit dieser Methode wird ein neues Spiel gestartet, indem inits() aufgerufen wird
	 */
	private void startNewGame()
	{
		this.blöcke = null;
		this.bälle = null;
		this.lebenList = null;
		this.spieler = null;
		this.inits();
		
	}
	
	/**
	 * diese methode setzt das spiel "gameOver" und pausiert das spiel.
	 */
	private void gameOver()
	{
		this.gameOver = true;
		pauseGame();	}
	
	/**
	 * hier werden die Blöcke, die im "löschen"-vektor sind aus dem blöcke- vektor entfernt
	 * Dannach wird der "löschen"- vektor mit einem neuen, leeren, vektor initialisiert.
	 */
	private void deleteBlocks()
	{
		for(Sprite k: löschen_blocks)
			blöcke.remove(k);
		
		löschen_blocks = new Vector<Sprite>();
	}
	
	/**
	 * hier werden die Leben aus dem Vektor "lebenList" gelöscht, die sich im "löschen_leben"- vektor befinden
	 */
	private void deleteLives()
	{
		for(Sprite k: löschen_leben)
			lebenList.remove(k);
	}
	
	
	/**
	 * Diese Methode zeichnet die Objekte mit dem Übergebenen Graphics- kontext
	 */
	public void zeichneObjekte(Graphics g)
	{
		for(Sprite b: spieler)
			b.zeichneDich(g);
		
		for(Ball s: bälle)
			s.zeichneDich(g);
		
		for(Sprite s: blöcke)
				s.zeichneDich(g);
		
		for(Sprite s: lebenList)
			s.zeichneDich(g);
		
		if(pauseGame == true && gameOver == false)
			showPause(g);
		
		if(gameOver == true)
			showGameOver(g);
	}
	
	private void showGameOver(Graphics g)
	{
		gameOverPic.zeichneDich(g);
	}
	
	private void showPause(Graphics g)
	{
		pause.zeichneDich(g);
	}
	
	/**
	 * die main- Funktion!
	 * @param args
	 */
	public static void main(String args[])
	{
		GamePanel spielfeld = new GamePanel(800, 600);
		t = new Thread(spielfeld);
		t.start();
		
	}
	
	
	
	
}//end class
