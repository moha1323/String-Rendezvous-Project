# String-Rendezvous-Project

**Rendezvous**
StringHandoff is used to pass a String from one thread to another. The passer and the receiver meet inside an instance for the hand-off. For example, if pass() is called and there is no receiver waiting, the thread calling pass() will block until a receiver arrives. Similarly, if receive() is called and there is no passer waiting, the thread calling receive() will block until a passer arrives.
**Timeouts Cause a TimedOutException to be Thrown**
For this class, methods that can timeout do not return true for success and false for timeout. Methods that take a timeout parameter (pass(String, long) or receive(long)) will throw a TimedOutException if the specified number of milliseconds passes without the hand-off occurring.
If the hand-off has completed—regardless of how long it took—no TimedOutException should be thrown.
A timeout value of 0L (zero) is used to indicate that the calling thread should wait indefinitely for the condition to be met (never timeout).
Negative timeouts should be tolerated and be treated as a very short timeout. TimedOutException is a RuntimeException.
**One Passer at a Time, One Receiver at a Time**
There can only be one thread waiting to pass at any given time. If threadA is waiting inside pass() and threadB calls pass(), threadB must throw an IllegalStateException. threadB throwing an exception should not affect threadA at all (threadA should continue waiting).
Similarly, there can only be one thread waiting to receive at any given time. If threadA is waiting inside receive() and threadB calls receive(), threadB must throw an IllegalStateException. threadB throwing an exception should not affect threadA at all
(threadA should continue waiting). IllegalStateException is a RuntimeException.
**Shutdown**
The methods that declare that they may throw a ShutdownException will do so after shutdown() has been called. The shutdown() method itself does not ever throw ShutdownException (in fact, it doesn't ever throw any kind of exception). The shutdown() method may be called multiple times and calling shutdown() multiple times must be harmless.
After threadA has already called shutdown(), if threadB calls pass(String), pass(String, long), receive(), or receive(long) threadB must immediately throw ShutdownException.
If threadA is waiting inside pass(String), pass(String, long), receive(), or receive(long) and threadB calls shutdown(), threadA should stop waiting and throw ShutdownException.
ShutdownException is a RuntimeException.
**Exception Precedence**
Exception precedence from highest to lowest is listed below:
1. InterruptedException 2. ShutdownException
3. IllegalStateException 4. TimedOutException
Example 1: while threadA is waiting, if both of these happen before threadA can re-acquire the lock to get back from wait: i) threadB calls shutdown() and then ii) threadC interrupts threadA, the result should be that threadA throws InterruptedException.
Example 2: while threadA is waiting with a timeout, if both of these happen before threadA can re-acquire the lock to get back from wait: i) threadB calls shutdown() and then ii) threadA times out, the result should be that threadA throws ShutdownException.
Example 3: while threadA is waiting inside pass(), if both of these happen before threadA can re-acquire the lock to get back from wait: i) threadB calls shutdown() and then ii) threadC calls pass(), the result should be that threadC should throw ShutdownException< (not IllegalStateException!) AND threadA should throw ShutdownException.
