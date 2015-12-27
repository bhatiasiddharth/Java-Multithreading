import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/******* THIS CLASS ENCAPSULATE DEALER'S LOGIC *********/
class Dealer implements Runnable {
	
	private GameData gameData; //shared data 
	private int numberAnnounced = 0; //it is set when a button on GUI is pressed
		
	/* **** DO NOT MODIFY **** this label is used by the dealer to set the game status */
	public final JLabel lblGameStatus = new JLabel();   
	
	public Dealer(GameData gameData) {
		this.gameData = gameData;			
		lblGameStatus.setAlignmentX(JLabel.CENTER_ALIGNMENT);		
	}
	
	public void run() {
		
		/* STEP-1: code to take a lock on gameData using lock1*/ 
		synchronized(gameData.lock1)
		{
			// dealer executes until either (or both) players declare success 
			/* STEP-2: condition for player1 and condition for player2 */ 
			while(gameData.playerSuccessFlag[0]== false && gameData.playerSuccessFlag[1]== false)
			{
				// set number announced flag to false before announcing the number
				gameData.noAnnouncedFlag = false;
				
				// set checked flag of both players as false before the number is announced
				gameData.playerChanceFlag[0] = false;
				gameData.playerChanceFlag[1] = false;
		
				/* STEP-3: code to take a lock on gameData using lock2 and wait while 
				 * no number has been pressed by the user on the GUI (See actionPerformed
				 * method of the GameGUI class 
				 * until the number is not announced the variable numberAnnounced 
				 * remains 0 (zero)
				 */
				
				synchronized(gameData.lock2)
				{
					while(numberAnnounced==0)
					{
						try {
							gameData.lock2.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				
				// STEP-4: initialize the announcedNumber in GameStat with the 
				// number pressed on GameGUI for the players to read
				gameData.announcedNumber=numberAnnounced; 
				
				// STEP-5: reset the announced number
				numberAnnounced = 0;
				  
				// STEP-6: communicate to the players that the number is announced
				// using one of the variables in GameData
				gameData.noAnnouncedFlag=true; 
								
				// STEP-7: notify all the players waiting for the number to be announced 
				// by the dealer using lock1 of GameData
				gameData.lock1.notifyAll();
								
				// STEP-8: wait using lock1 of GameData while the players haven't checked 
				// the numbers 			
				while(gameData.playerChanceFlag[0] == false || gameData.playerChanceFlag[1] == false )
				{
					try {
						gameData.lock1.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			}
			
			// Check if Player1 has won
			if (gameData.playerSuccessFlag[0]== true && gameData.playerSuccessFlag[1]== false)
			{ 
				lblGameStatus.setText("PLAYER-1 HAS WON");				
			} 
			// Check if Player2 has won
			if(gameData.playerSuccessFlag[1]== true && gameData.playerSuccessFlag[0]== false)
			{ 
				lblGameStatus.setText("PLAYER-2 HAS WON");				
			} 
			// Check if both Player1 and Player2 have won
			/* STEP-11: specify condition */ 
			if(gameData.playerSuccessFlag[0]== true && gameData.playerSuccessFlag[1]== true)
				
			{
				lblGameStatus.setText("BOTH PLAYER-1 AND PLAYER-2 HAVE WON");				
			}
			
			gameData.gameCompleteFlag = true; // Set the complete flag to true 
			gameData.lock1.notifyAll(); // If at all any player is waiting			
		}		
	}

	public void setAnnouncedNumber(int i) {
		this.numberAnnounced = i;	
	}
}

/******* THIS CLASS ENCAPSULATE PLAYER'S LOGIC *********/
class Player implements Runnable {

	private int id;							// player id [0 or 1]
	private GameData gameData;				// shared object
	private JPanel playerTicketPanel;		// GUI component
	private JButton[] btnOnTicket;			// buttons on player ticket
	private int totalNumbersFound;    		// total numbers found
	private final static int MAXNO = 6;		// maximum numbers on player ticket
	
	// stores the numbers on the player ticket
	private int[] ticket = new int[MAXNO];
			
	public Player(GameData gameData, int id) { 
		
		this.id = id; 		
		this.gameData = gameData;	
		this.totalNumbersFound = 0;
		
		// randomly generate six numbers and store them in the lstTicket
		for(int i = 0; i < MAXNO; i++) {
			int p = randInt(i*5 + 1, (i+1) * 5);
			ticket[i] = p;
		}
		
		// initialize player panel
		playerTicketPanel = new JPanel();
		// set playerPanel layout
		playerTicketPanel.setLayout(new GridLayout(1,6));
		// create an array of six buttons 
		btnOnTicket = new JButton[MAXNO];
		
		// initialize the buttons on ticket and add them to playerPanel
		for(int i = 0; i < MAXNO; i++) {
			btnOnTicket[i] = new JButton(String.valueOf(ticket[i]));
			btnOnTicket[i].setEnabled(false);
			playerTicketPanel.add(btnOnTicket[i]);
		}
	}
	
	private static int randInt(int min, int max) {	//method to generate random numbers
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void run() {
		/* STEP-12: write code to take a lock on gameData using lock1 */ 
		synchronized(gameData.lock1)
		
		{			
			// both players execute while the game is not complete
			/* STEP-13: Specify condition */ 
			while(gameData.gameCompleteFlag == false)
			{
			
				// STEP-14: both players should wait using lock1 of GameData until a number 
				// is announced by the dealer or its not the chance of the player  
				while(gameData.noAnnouncedFlag=false || gameData.playerChanceFlag[id] == true )
				{
					try {
						gameData.lock1.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
												
				// Its important to check this condition again because it is possible that
				// one player may have found all the numbers when the other was waiting
				if(!gameData.gameCompleteFlag) {					
					
					// STEP-15: Check if the announced number is on the player's ticket
					// if the number is found, the player increments the totalNumbersFound
					// and set the back ground color of the button to GREEN using the following statement
					
					
					for(int i=0;i<MAXNO;i++)
					{
						if(ticket[i]==gameData.announcedNumber)
						{
							totalNumbersFound++;
							this.btnOnTicket[i].setBackground(Color.GREEN);
								
						}
							
					}
										
					// STEP-16: player checks if it has won the game i.e., it has found all numbers
					// then it should report success
					if(totalNumbersFound==MAXNO)
					{
						gameData.playerSuccessFlag[id]= true; 
					}
					
										
					// player sets its chance flag 
					gameData.playerChanceFlag[id] = true;
					
					// STEP-17: notify all others waiting on lock1 of GameData
					gameData.lock1.notifyAll();
				}
			}
		}
	}

	public JPanel getPlayerTicketPanel() {		
		return playerTicketPanel;
	}
}

/**** THE INSTANCE OF THIS CLASS IS USED AS A MEANS OF COMMUNICATION *
 * AND SYNCHRONIZTION BETWEEN THE PLAYER AND DEALER THREADS ********** 
 ****/
class GameData {
	public int announcedNumber = 0;	 
	public boolean gameCompleteFlag = false;	
	public boolean noAnnouncedFlag = false;
	public boolean[] playerSuccessFlag = new boolean[2];
	public boolean[] playerChanceFlag = new boolean[2];
	
	public Object lock1 = new Object();
	public Object lock2 = new Object();
}

/**** THIS CLASS ENCAPSULATES THE GUI OF THE APPLICATION****/
class GameGUI implements ActionListener{
	
	private Dealer dealer;
	private GameData gameData;	
	private JButton[] btnDealerBoardNumbers;	
	
	GameGUI(GameData gameData, Dealer dealer, Player player1, Player player2) {
		
		this.dealer = dealer;
		this.gameData = gameData;
				
		JFrame mainGameFrame = new JFrame("MINI TAMBOLA");
		mainGameFrame.setSize(400,400);
		mainGameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblDealer = new JLabel("Dealer",JLabel.CENTER);
		mainGameFrame.setLayout(new BoxLayout(mainGameFrame.getContentPane(),BoxLayout.Y_AXIS));		
		mainGameFrame.add(lblDealer);
		
		// Panel for Dealer buttons
		JPanel dealerPanel = new JPanel();
		dealerPanel.setLayout(new GridLayout(6,5));
		
		// initialize dealer board number buttons
		btnDealerBoardNumbers = new JButton[30];
		
		for(int i = 0; i < 30; i++) {
			btnDealerBoardNumbers[i] = new JButton(String.valueOf(i+1));
			btnDealerBoardNumbers[i].addActionListener(this);
			dealerPanel.add(btnDealerBoardNumbers[i]);
		}
		
		mainGameFrame.add(dealerPanel);
		
		JLabel lblPlayer1 = new JLabel("Player1",JLabel.CENTER);
		mainGameFrame.add(lblPlayer1);
		mainGameFrame.add(player1.getPlayerTicketPanel());// Add player1 ticket
		
		JLabel lblPlayer2 = new JLabel("Player2",JLabel.CENTER);
		mainGameFrame.add(lblPlayer2);
		mainGameFrame.add(player2.getPlayerTicketPanel());// Add player2 ticket
		
		mainGameFrame.add(dealer.lblGameStatus);
		
		mainGameFrame.setVisible(true);
	}

	/* Action taken when the user presses a button on the dealer board */
	public void actionPerformed(ActionEvent e) {
		// we will have to run a for loop 30 times for each time a button is pressed 
		// we have to identify which button has raised an event
		for(int i = 0; i < 30; i++) {			
			if(e.getSource() == btnDealerBoardNumbers[i]) {				
				// this thread will take a lock on the game object  
				synchronized(gameData.lock2) {									
					dealer.setAnnouncedNumber(i+1);
					btnDealerBoardNumbers[i].setForeground(Color.gray);
					btnDealerBoardNumbers[i].setEnabled(false);
					gameData.lock2.notify();
				}				
				break;
			}
		}
	}		
}

/**** THIS CLASS HAS THE main() METHOD****/
class GameApp {

	public static void main(String[] args) {
		
		final GameData game  = new GameData();
		final Dealer dealer  = new Dealer(game);
		final Player player1 = new Player(game, 0);
		final Player player2 = new Player(game, 1);
		
		Thread dealerThread  = new Thread(dealer );
		Thread player1Thread = new Thread(player1);
		Thread player2Thread = new Thread(player2);
		
		dealerThread. start();
		player1Thread.start();
		player2Thread.start();
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new GameGUI(game,dealer,player1,player2);
			}
		});		
	}
}

