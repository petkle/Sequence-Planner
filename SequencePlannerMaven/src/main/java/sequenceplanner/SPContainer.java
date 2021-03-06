package sequenceplanner;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeListener;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.AutomataVerifier;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynchronizationType;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.AutomataVerificationWorker;
import sequenceplanner.efaconverter.IdentifyOpRelations;
import sequenceplanner.efaconverter.SPtoAutomatonConverter;
import sequenceplanner.model.ConvertFromXML;
import sequenceplanner.model.ConvertToXML;
import sequenceplanner.model.Model;
import sequenceplanner.model.TreeNode;
import sequenceplanner.model.data.ViewData;
import sequenceplanner.view.AbstractView;
import sequenceplanner.view.operationView.Constansts;
import sequenceplanner.view.operationView.OperationView;
import sequenceplanner.view.resourceView.ResourceView;
import sequenceplanner.view.treeView.TreeView;
import sequenceplanner.xml.SequencePlannerProjectFile;

/* TODO
 *  - Placement when inserting into groupCell
 *  - Save alternative in a correct way
 *  - Make correct edges when inserting cell in cell.
 *  - Drag'n'Drop
 *
 *
 * /


/**
 *
 * @author Erik Ohlson
 */
public class SPContainer extends JPanel {

   private static int viewCounter = 0;

   // Container for most of the views
   protected JTabbedPane viewPane;

   // Container for project / Library views
   protected JSplitPane projectPane;

   //File, if this project is saved so far
   File projectFile;

   // Referense to the main data model
   Model model;

   //Filefilter for the project
   private static final FileFilter filter = new FileFilter() {

      @Override
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".sopx") || f.isDirectory();
      }

      @Override
      public String getDescription() {
         return "Sequence Planner Project File";
      }
   };

   public SPContainer() {
      this.model = new Model();

      initializePanes();

      createOperationView("Free view " + Integer.toString(++viewCounter));


   }

   private void initializePanes() {
      viewPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT) {

         @Override
         public void add(Component component, Object constraints) {
            super.add(component, new CustomTabComponent(SPContainer.this, component));
            setTabComponentAt(indexOfComponent(component), new CustomTabComponent(SPContainer.this, component));
            setSelectedComponent(component);
         }
      };

      viewPane.addChangeListener(new ChangeListener() {

         @Override
         public void stateChanged(ChangeEvent e) {
            Component c = viewPane.getSelectedComponent();

            Component prev = projectPane.getBottomComponent();
            if (prev != null) {
               prev.setPreferredSize(new Dimension(prev.getWidth(), prev.getHeight()));
            }

            if (c != null) {
               JComponent input = ((AbstractView) c).getOutline();
               projectPane.setBottomComponent(input);
            } else {
               projectPane.setBottomComponent(null);
            }
            projectPane.resetToPreferredSizes();


         }
      });

      projectPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, new TreeView(this), new JPanel());
      projectPane.setDividerSize(3);
      projectPane.setResizeWeight(1.0);

      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            false, projectPane, viewPane);


      this.setLayout(new BorderLayout());
      this.add(split, BorderLayout.CENTER);

      this.add(initMenuPanel(), BorderLayout.NORTH);
   }

   public void createResourceView(TreeNode root) {
      String name = root.getNodeData().getName();
      viewPane.add(new ResourceView(this, root, name), name);

   }

   public OperationView createOperationView(String name) {
      OperationView ov = new OperationView(this, name);
      viewPane.add(ov, name);
      return ov;
   }

   public boolean createOperationView(ViewData d) {
      OperationView ov = new OperationView(this, d);
      for (int i = 0; i < viewPane.getTabCount(); i++) {
         Component c = viewPane.getComponentAt(i);

         if (c instanceof OperationView &&
               ((OperationView) c).getName().equals(d.getName())) {

            viewPane.setSelectedIndex(i);
            return true;
         }
      }

      viewPane.add(ov, d.getName());
      return true;
   }

   public Model getModel() {
      return model;
   }

   public void close(Component c) {
      if (c instanceof AbstractView) {
         if (((AbstractView) c).closeView()) {
            viewPane.remove(c);
         }

      }
   }

   private JMenuBar initMenuPanel() {
      JMenuBar mb = new JMenuBar();

      JMenu file = new JMenu("File");
      mb.add(file);

      JMenuItem newOperationView = new JMenuItem(
            new AbstractAction("Create new OperationView") {

               @Override
               public void actionPerformed(ActionEvent e) {

                  createOperationView("Free view " + Integer.toString(++viewCounter));
               }
            });

      file.add(newOperationView);

      newOperationView = new JMenuItem(
            new AbstractAction("Create new ResourceView") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  createResourceView(model.getResourceRoot());
               }
            });

      file.add(newOperationView);

      newOperationView = new JMenuItem(
            new AbstractAction("Exit") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  System.exit(0);
               }
            });


      file.add(newOperationView);

      /////////////////////////////////////////
      //       Start of EDIT menu            //
      /////////////////////////////////////////

      JMenu edit = new JMenu("Edit");
      mb.add(edit);

      JMenuItem pref = new JMenuItem(
            new AbstractAction("Preferences") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  PreferencePane pref = new PreferencePane();
                  Point p = getParent().getLocation();

                  pref.setLocation(p.x + 300, p.y + 250);

                  pref.setVisible(true);

               }
            });
      edit.add(pref);

      JMenuItem addAll = new JMenuItem(
            new AbstractAction("AddAllCellsToNewView") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  OperationView v = createOperationView("ViewAll");
                  v.open(model.getChildren(model.getOperationRoot()));
               }
            });
      edit.add(addAll);


      /////////////////////////////////////////
      //       End of EDIT menu            //
      /////////////////////////////////////////

      //////////////////////////////////////
      //       Start of ProjectMenu         //
      //////////////////////////////////////

      JMenu project = new JMenu("Project");
      JMenuItem open = new JMenuItem(
            new AbstractAction("Open") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  openModel();
               }
            });


      project.add(open);

      JMenuItem save = new JMenuItem(
            new AbstractAction("Save") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  saveModel(false);
               }
            });

      project.add(save);

      JMenuItem saveAs = new JMenuItem(
            new AbstractAction("Save as") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  saveModel(true);
               }
            });

      project.add(saveAs);


      JMenuItem close = new JMenuItem(
            new AbstractAction("Close") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  System.exit(0);
               }
            });


      project.add(close);

      mb.add(project);
      //////////////////////////////////////
      //       End of ProjectMenu         //
      //////////////////////////////////////

      JMenu compile = new JMenu("Convert");

      compile.add(newOperationView = new JMenuItem(
            new AbstractAction("Verify nonblocking") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  // The user clicked on Create EFA


                  // Create a module of EFAs with Sequence Planner SOP as input


                  Runnable t = new Runnable() {

                     @Override
                     public void run() {
                        try {

                           ProjectBuildFromWaters projectBuilder = new ProjectBuildFromWaters(new DocumentManager());
                           SPtoAutomatonConverter converter = new SPtoAutomatonConverter(model, 1);
                           ModuleSubject moduleSubject = converter.getModule().getModule();
                           Project efaProject = projectBuilder.build(moduleSubject);


                           VerificationOptions vo = new VerificationOptions();
                           vo.setVerificationType(VerificationType.NONBLOCKING);
                           vo.setAlgorithmType(VerificationAlgorithm.MONOLITHIC);

                           SynchronizationOptions so = new SynchronizationOptions();
                           so.setSynchronizationType(SynchronizationType.FULL);

                           MinimizationOptions mo = new MinimizationOptions();

                           AutomataVerifier av = new AutomataVerifier(efaProject, vo, so, mo);
                           Boolean success = av.verify();

                           JOptionPane.showInternalMessageDialog(SPContainer.this, (success ? "System is Non-blocking": "System is blocking"));


                        } catch (Exception t) {
                           t.printStackTrace();
                        }
                     }
                  };


                  SwingUtilities.invokeLater(t);



               }
            }));

      compile.add(newOperationView = new JMenuItem(
            new AbstractAction("Save EFA as file (optimization)") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  // The user clicked on Create EFA


                  // Create a module of EFAs with Sequence Planner SOP as input
                  try {
                     SPtoAutomatonConverter converter = new SPtoAutomatonConverter(model, 1);

                     ModuleSubject moduleSubject = converter.getModule().getModule();
                     moduleSubject.setName("Sequence Planner to EFA output");

                     String filepath = "";
                     JFileChooser fc = new JFileChooser("C:\\Documents and Settings\\EXJOBB SOCvision\\Desktop");
                     int fileResult = fc.showSaveDialog(null);
                     if (fileResult == JFileChooser.APPROVE_OPTION) {
                        filepath = fc.getSelectedFile().getAbsolutePath();

                        File file = new File(filepath);

                        file.createNewFile();

                        ModuleSubjectFactory factory = new ModuleSubjectFactory();

                        //Save module to file

                        JAXBModuleMarshaller marshaller =
                              new JAXBModuleMarshaller(factory,
                              CompilerOperatorTable.getInstance());

                        marshaller.marshal(moduleSubject, file);

                     }


                  } catch (Exception t) {
                     t.printStackTrace();
                  }





               }
            }));

      compile.add(newOperationView = new JMenuItem(
            new AbstractAction("Save EFA as file (reset)") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  // The user clicked on Create EFA

//                        // Create a module of EFAs with Sequence Planner SOP as input
                  try {
                     SPtoAutomatonConverter converter = new SPtoAutomatonConverter(model, 2);

                     ModuleSubject moduleSubject = converter.getModule().getModule();
                     moduleSubject.setName("Sequence Planner to EFA output");

                     String filepath = "";
                     JFileChooser fc = new JFileChooser("C:\\Documents and Settings\\EXJOBB SOCvision\\Desktop");
                     int fileResult = fc.showSaveDialog(null);
                     if (fileResult == JFileChooser.APPROVE_OPTION) {
                        filepath = fc.getSelectedFile().getAbsolutePath();

                        File file = new File(filepath);

                        file.createNewFile();

                        ModuleSubjectFactory factory = new ModuleSubjectFactory();

                        //Save module to file

                        JAXBModuleMarshaller marshaller =
                              new JAXBModuleMarshaller(factory,
                              CompilerOperatorTable.getInstance());

                        marshaller.marshal(moduleSubject, file);

                     }


                  } catch (Exception t) {
                     t.printStackTrace();
                  }

               }
            }));

      compile.add(newOperationView = new JMenuItem(
            new AbstractAction("Save cost automata as file") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  // The user clicked on Create EFA
                  try {
                     SPtoAutomatonConverter converter = new SPtoAutomatonConverter(model, 1);

                     Automata costAutomata = converter.getCostAutomata();

                     String filepath = "";
                     JFileChooser fc = new JFileChooser("C:\\Documents and Settings\\EXJOBB SOCvision\\Desktop");
                     int fileResult = fc.showSaveDialog(null);
                     if (fileResult == JFileChooser.APPROVE_OPTION) {
                        filepath = fc.getSelectedFile().getAbsolutePath();

                        File fil = new File(filepath);

                        fil.createNewFile();

                        AutomataToXML a = new AutomataToXML(costAutomata);

                        a.serialize(filepath);

                     } else {
                        System.out.println("User clicked cancel...");
                     }
                  } catch (Exception w) {
                     w.printStackTrace();
                  }


               }
            }));
      compile.add(newOperationView = new JMenuItem(
            new AbstractAction("Save optimal automaton as file") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  // The user clicked on Create EFA

                  try {
                     SPtoAutomatonConverter converter = new SPtoAutomatonConverter(model, 5);

                     Automata totalAutomata = converter.getTotalAutomata();

                     Automaton optimizedAutomaton = converter.optimizeAutomata(totalAutomata);


                     String filepath = "";
                     JFileChooser fc = new JFileChooser("C:\\Documents and Settings\\EXJOBB SOCvision\\Desktop");
                     int fileResult = fc.showSaveDialog(null);
                     if (fileResult == JFileChooser.APPROVE_OPTION) {
                        filepath = fc.getSelectedFile().getAbsolutePath();

                        File fil = new File(filepath);

                        fil.createNewFile();

                        AutomataToXML a = new AutomataToXML(optimizedAutomaton);

                        a.serialize(filepath);

                     }

                  } catch (Exception q) {
                     q.printStackTrace();
                  }

               }
            }));


      compile.add(newOperationView = new JMenuItem(
            new AbstractAction("Identify Relations") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  // The user clicked on Create EFA

                  try {
                      IdentifyOpRelations idOp = new IdentifyOpRelations();
                      idOp.identifyRelations(model);

                  } catch (Exception q) {
                     q.printStackTrace();
                  }

               }
            }));            


      mb.add(compile);


      return mb;
   }

   public boolean openModel() {
      JFileChooser fc = new JFileChooser("user.dir");

      fc.setFileFilter(filter);
      int answer = fc.showOpenDialog(null);


      if (answer == JFileChooser.APPROVE_OPTION) {
         closeAllOpenWindows();
         openModel(fc.getSelectedFile());
         model.reloadNamesCache();
         try {
            ViewData toOpen = (ViewData) model.getViewRoot().getChildAt(0).getNodeData();
            createOperationView(toOpen);

         } catch (ClassCastException e) {
            System.out.println("Could not cast first child of viewroot to viewData");
         }
         return true;
      }
      return false;
   }

   public void closeAllOpenWindows() {
      for (int i = 0; i < viewPane.getTabCount(); i++) {
         close(viewPane.getComponentAt(i));

      }

   }

   public boolean openModel(File inputFile) {

      SequencePlannerProjectFile project = null;

      try {
         javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(SequencePlannerProjectFile.class.getPackage().getName());
         javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
         project = (SequencePlannerProjectFile) unmarshaller.unmarshal(inputFile);

      } catch (javax.xml.bind.JAXBException ex) {
         java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
         return false;
      } catch (ClassCastException ex) {
         System.out.println("Class Cast Error in openModel");
         return false;
      }


      ConvertFromXML con = new ConvertFromXML(model);
      model = con.convert(project);

      model.rootUpdated();

      return false;
   }

   // sopx
   public boolean saveModel(boolean saveAs) {

      if (projectFile == null && !saveAs) {
         saveAs = true;
      }

      if (saveAs) {
         String filepath = "";

         JFileChooser fc = new JFileChooser("user.dir");
         fc.setFileFilter(filter);

         int fileResult = fc.showSaveDialog(null);

         if (fileResult == JFileChooser.APPROVE_OPTION) {
            filepath = fc.getSelectedFile().getAbsolutePath();

            filepath = filepath.endsWith(Constansts.FILEFORMAT) ? filepath : filepath + Constansts.FILEFORMAT;

            if (filepath.endsWith(Constansts.FILEFORMAT)) {

               projectFile = saveModelToFile(filepath);
               return true;
            }
         }
      } else {
         return saveModelToFile(projectFile);
      }

      return false;
   }

   public void saveBackup() {
      if (projectFile != null) {
         String path = projectFile.getParent();
         path = path + File.separatorChar + "backup";

         File f = new File(path);
         f.mkdir();

         Calendar c = Calendar.getInstance();
         String date = c.get(Calendar.YEAR) + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR_OF_DAY) + "" + c.get(Calendar.MINUTE) + "" + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND);

         path = path + File.separatorChar + projectFile.getName() + "_" + date + Constansts.FILEFORMAT;
         saveModelToFile(path);
      }
   }

   public File saveModelToFile(String filepath) {
      File file = new File(filepath);

      try {
         file.createNewFile();
         saveModelToFile(file);
         return file;

      } catch (IOException ex) {
         System.out.println("File save error\n " + ex.getMessage());
         return null;
      }
   }

   public boolean saveModelToFile(File file) {
      ConvertToXML converter = new ConvertToXML(model);
      SequencePlannerProjectFile project = converter.convert();

      try {
         javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(project.getClass().getPackage().getName());
         javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
         marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
         marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(project, file);
         return true;

      } catch (javax.xml.bind.JAXBException ex) {
         System.out.println("File save error\n " + ex.getMessage());
         return false;
      }
   }
}
