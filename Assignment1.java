
class BoundedBuffer
{
	private int nextIn;				//Pointer to next valid empty cell
	private int nextOut;				//Pointer to next valid occupied cell
	private int occupied;				//Number of occupied cells
	private int ins;				//Number of inserts
	private int outs;				//Number of removals
	private boolean dataAvailable;		//Is data available?
	private boolean roomAvailable;		//Is buffer full?
	private int [] buffer;			//Buffer for storing integers
	
	public BoundedBuffer(int size)
	{
		buffer = new int[size];		//Create buffer of length == size
		ins = 0;				//No ins 
		outs = 0;				//No outs
		dataAvailable = false;			//No data available
		roomAvailable = true;			//Room available
		nextIn = 0;				//Take from buffer[0]
		nextOut = 0;				//Put to buffer[0]
	}
	
	public synchronized void insertItem(int item)	
	{							//accessor adder method to buffer object. 				
		while(!roomAvailable)				//while no room available
		{
			try
			{
				wait();				//wait until notified;
			} 
			catch (InterruptedException e) { }
		}		
	
		buffer[nextIn++] = item;			//save value at [nextIn] and increment pointer
		if(nextIn == buffer.length)			//if pointer to next item == end of the buffer
		{
			nextIn = 0;				//set pointer to the beginning
		}
		
		occupied++;					//increment number of occupied cells
		ins++;						//increment number of inserts
		
		if(occupied == buffer.length) 			//if no. occupied cells == length of buffer
		{
			roomAvailable = false;			//set room available to false;
		}
			
		if(!dataAvailable)				//if no data available
		{
			dataAvailable = true;			//set dataAvaiable to true
			notifyAll();				//notify all on wait queue
		}		
	}
	
	public synchronized int removeItem()			
	{							//Synchronized accessor remove method to buffer object	
		while(!dataAvailable)				//while no data available
		{
			try	
			{
				wait();				//wait until notified
			}
			catch(InterruptedException e) { }
		}
		
		occupied--;					//decrement number of occupied cells
		outs++;						//decrement number of removals
		
		int toReturn = buffer[nextOut++];		//save value from buffer[nextOut]
								//increment the pointer
		
		if(nextOut == buffer.length)			//if pointer to next value to be removed == buffer.length
		{
			nextOut = 0;				//set it to the beginning of the buffer
		}
		
		if(occupied == 0)				//if number of occupied cells == 0
		{
			dataAvailable = false;			//set dataAvailable to false
		}
		
		if(!roomAvailable)				//if room available == false
		{
			roomAvailable = true;			//set roomAvailable to true
			notifyAll();				//notify all on the wait queue
		}
		
		return toReturn;				//return value stored
	}
	
	public synchronized String getInfo()
	{
		return "Delta = " + (ins - outs - occupied) + " Occupied = " + occupied;
	}
	
	public synchronized int outs()
	{
		return outs;
	}
}

class Producer extends Thread
{
	private BoundedBuffer buffer;
	
	Producer(BoundedBuffer buffer)
	{
		this.buffer = buffer;
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				buffer.insertItem((int) (Math.random() * 100));	//insert a value to buffer
				sleep((int) (Math.random() * 100));			//sleep for random amount of time
			}
			catch(InterruptedException e) 
			{
				System.out.println("Goodbye from producer");
				return;
			} 
		}
	}
}

class Consumer extends Thread
{
	private BoundedBuffer buffer;
	long startTime = 0;
	
	Consumer(BoundedBuffer buffer)
	{
		this.startTime = System.currentTimeMillis();			//save starting time
		this.buffer = buffer;						
	}
	
	public void run()
	{
		while(true)
		{
			try 
			{
				buffer.removeItem();				//remove value from a buffer
				sleep((int) (Math.random() * 100));		//sleep for random amount of time
			} 
			catch (InterruptedException e) 
			{
				System.out.println("Goodbye from consumer");
				System.out.println("Average wait is "+ ((float) buffer.outs() / (System.currentTimeMillis() - startTime) * 10000));
				return;
			}
		}
	}
}

class Watcher extends Thread
{
	private BoundedBuffer buffer;
	
	Watcher(BoundedBuffer buffer)
	{
		this.buffer = buffer;
	}
	
	public void run()
	{
		while(true)
		{
			try 
			{
				System.out.println(buffer.getInfo());	//print info about buffer
				sleep(1000);				//sleep for 1 second
			}
			catch (InterruptedException e) 
			{
				System.out.println("Watcher exiting");
				return;
			}
		}
	}
}

public class Assignment1
{
	public static void main(String [] args)
	{
		
		BoundedBuffer buffer = new BoundedBuffer(30);		//Create buffer
		
		Producer producer = new Producer(buffer);		//Create new threads
		Consumer consumer = new Consumer(buffer);
		Watcher watcher = new Watcher(buffer);
		
		consumer.start();					//Start all threads
		producer.start();
		watcher.start();
			
		try 
		{	
			watcher.join(60000);				//Join on watcher for 1 minute.					
		} 
		catch (InterruptedException e) 
		{
			System.out.println("JOIN INTERRUPTED!");
		}		
		
		watcher.interrupt();					//Interrupt threads, return from run()
		consumer.interrupt();
		producer.interrupt();
	}
}
