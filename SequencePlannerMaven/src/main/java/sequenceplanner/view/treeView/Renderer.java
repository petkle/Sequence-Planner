/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sequenceplanner.view.treeView;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.log4j.Logger;
import sequenceplanner.SequencePlanner;
import sequenceplanner.model.Model;
import sequenceplanner.model.TreeNode;
import sequenceplanner.model.data.Data;

/**
 *
 * @author erik
 */
public class Renderer extends DefaultTreeCellRenderer {

   static Logger logger = Logger.getLogger( Renderer.class );

   private ImageIcon folderIcon;
   private ImageIcon folderOpenIcon;
   private ImageIcon operationIcon;
   private ImageIcon resourceIcon;
   private ImageIcon variableIcon;
   private ImageIcon liasonIcon;
   private ImageIcon viewIcon;

   public Renderer() {

      //Register all icons used with the icons.
      folderIcon = SequencePlanner.getNewIcon( "resources/icons/folder.png");
      folderOpenIcon = SequencePlanner.getNewIcon( "resources/icons/folder-open.png");
      operationIcon = SequencePlanner.getNewIcon( "resources/icons/sop.png");
      resourceIcon = SequencePlanner.getNewIcon( "resources/icons/robot.png");
      variableIcon = SequencePlanner.getNewIcon("resources/icons/variable.png");
      liasonIcon = SequencePlanner.getNewIcon( "resources/icons/icon.png");
      viewIcon = SequencePlanner.getNewIcon( "resources/icons/face-monkey.png");
   }

   @Override
   public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
      super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

      TreeNode node = (TreeNode) value;
      Data data = node.getNodeData();

      if (Model.isOperation( data )) {
         setIcon( operationIcon);
         
      } else if (Model.isFolder( data )) {
         ImageIcon folder = expanded ? folderOpenIcon : folderIcon;
         setIcon( folder);

      } else if (Model.isResource( data )) {
         setIcon( resourceIcon);

      } else if (Model.isLiason( data )) {
         setIcon( liasonIcon );
      } else if (Model.isView( data )) {
         setIcon( viewIcon );
      } else if (Model.isVariable(data)) {
         setIcon(variableIcon);
      }

      return this;
   }


}
