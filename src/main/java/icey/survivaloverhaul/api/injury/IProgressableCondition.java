package icey.survivaloverhaul.api.injury;

/**
 * Interface for progressable conditions, such as diseases.
 * @author Icey
 */
public interface IProgressableCondition
{
	/**
	 * Gets the current progress of this condition.
	 * @return progress The current progress of the condition.
	 */
	public float getProgress();
	
	/**
	 * Sets the current condition's progress to a specified value.
	 * @param progress The value to be set to.
	 */
	public void setProgress(float progress);
	
	/**
	 * 
	 * @param progress The value to be added to the condition's progress. 
	 */
	public void addProgress(float progress);

	public void resetProgress();
}
