package sequenceplanner.view;

/**
 *
 * @author Erik Ohlson
 */
public interface IView {

   /**
    * Save the view instance to the modell
    *
    * @return if save was successfull
    */
   public void save(boolean newSave, boolean saveView);

}
