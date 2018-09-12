/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
 
package brya-n.ffmpeg_gui;
 
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
 
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
 
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.PumpStreamHandler;
 
import javax.swing.filechooser.*;
 
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.event.ListSelectionEvent;
 
 
/**
*
* @author brya-n
* User interface for command line arguments in ffmpeg
*/
public class InputOutput extends JPanel implements ActionListener, ListSelectionListener
{
   private JTextArea log;
   private JTextField showCommandText;
   private JScrollPane logScrollPane;
   private JSplitPane notesAndLists;
   private JButton ffmpegButn, resetButn, saveLogButn, exitButn, loadFileButn;    
   private JButton fileSelButn, projectClearButn, inputButn, outputButn; 
   private JButton setGlobalButn, setInputButn, setOutputButn, clearButn;
   private JButton convertButn, repeatButn, outputNameChange; 
   
   private File ffmpegFile, outputDirFile, sourceFile, projectFile;
   private File[] inputDirFile, projectFileList;
   private Set <File> sourceFileSet, projectFileSet;
   
   // additional for a list of arguments
   private ArrayList globalArgList, inputArgList, outputArgList;
   private String delimString;
   
   private String ffmpegDir, ffmpegDirAndFile, inputDir, outputDir;
   private String ffmpegFileName, outputLogName;
   private String[] inputFileName, projectFileName;
   
   private static final String NEWLINE = "\n";
   private static final String TAB = "\t";
   private static final String INDENT = "                          ";  
   private static final String WINPATHSEP = "\\";
   private String ffMpegRequest;
   private String globalRequest, inputRequest, outputRequest;
   
   private DefaultListModel sourceListModel, projectListModel;
   private JList sourceList, projectList;
   private JScrollPane sourceScroller, projectScroller;
     
            
   public InputOutput()
   {
      super(new BorderLayout());
      sourceFileSet = new HashSet();
      projectFileSet = new HashSet();
      
      log = new JTextArea(20, 48);
      // insets represent an object within the contained area, margins
      log.setMargin(new Insets(4, 4, 4, 4));
      // unable to edit the notes outside the program
      log.setEditable(false);
      log.setFont(new Font("Courier New", Font.PLAIN, 12));
      log.setToolTipText("Log of all main activities, unable to edit");
         
      // scroll pane for the notes
      logScrollPane = new JScrollPane(log);
          
      // buttons
      outputNameChange = new JButton ("Filename Changes");
      outputNameChange.setToolTipText("Output file amendments include: "
                                       + " prefix (before), "
                                       + " postfix (after), "
                                       + " change of file extension");
      outputNameChange.setEnabled(false);
      ffmpegButn = new JButton ("Locate ffmpeg.exe");
      ffmpegButn.setToolTipText("Navigate to ffmpeg.exe so the program knows what to use.");
      inputButn = new JButton ("Dir & Files for Input");
      inputButn.setToolTipText("Select a directory and also the file(s) you "
                                 + "need to recover.  These will be held below.");
      inputButn.setEnabled(false);
      outputButn = new JButton ("Directory for Log and output");
      outputButn.setToolTipText("Select a directory where the output files will"
                                 + " be saved to, and the log file.");
      outputButn.setEnabled(false);
      convertButn = new JButton ("One Pass"); 
      convertButn.setEnabled(false);
      convertButn.setToolTipText("Converts each file in the project once using the command");
      repeatButn = new JButton ("Multiple");
      repeatButn.setEnabled(false);
      repeatButn.setToolTipText("Converts each file multiple times according to:"
                                + NEWLINE + " Number of Times and Interval. ");
      saveLogButn = new JButton ("Save Log");
      saveLogButn.setToolTipText("Save the current log to a file in" 
                                 + " the output directory.");
      resetButn = new JButton ("Reset All");
      resetButn.setToolTipText("Clear all references apart from ffmpeg.exe");
      resetButn.setEnabled(false);
      exitButn = new JButton ("EXIT");
      exitButn.setToolTipText("Close the program down.");
      exitButn.setBackground(Color.red);
      
      loadFileButn = new JButton ("Get File(s)");
      loadFileButn.setEnabled(false);
      loadFileButn.setToolTipText("Selects all file(s) from source directory");
      fileSelButn = new JButton ("Select File");
      fileSelButn.setToolTipText("Adds the selected file to the project screen");
      fileSelButn.setEnabled(false);
      projectClearButn = new JButton ("Clear Project");
      projectClearButn.setToolTipText("Empties the Project of Files");
      projectClearButn.setEnabled(false);
      
      setGlobalButn = new JButton ("Global command");
      setGlobalButn.setToolTipText("Sets a global command for use with ffmpeg");
      setGlobalButn.setEnabled(false);
      setInputButn = new JButton ("Input command");
      setInputButn.setToolTipText("Sets the input command for use with ffmpeg");
      setInputButn.setEnabled(false);
      setOutputButn = new JButton ("Output command");
      setOutputButn.setToolTipText("Sets the output command for use with ffmpeg");
      setOutputButn.setEnabled(false);
      clearButn = new JButton ("Clear commands");
      clearButn.setToolTipText("Clears all stored commands, keeps project files");
      clearButn.setEnabled(false);
 
      showCommandText = new JTextField (48);
      showCommandText.setEditable(false);
      showCommandText.setFont(new Font("Courier New", Font.PLAIN, 12));
      showCommandText.setToolTipText("Displays the command that will be sent. "
                                    + " Because of multiple files possible, "
                                    + " the actual file will not be shown here.");
            
      // new source list code
      sourceListModel = new DefaultListModel();
      sourceList = new JList( sourceListModel );
      sourceList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
      sourceList.setFont(new Font("Courier New", Font.PLAIN, 12));
      sourceList.setToolTipText("Stores files retrieved.");
      sourceScroller = new JScrollPane ( sourceList );
      sourceScroller.setPreferredSize(new Dimension (300, 200));
      
      // new project list code
      projectListModel = new DefaultListModel();
      projectList = new JList( projectListModel );
      projectList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
      projectList.setFont(new Font("Courier New", Font.PLAIN, 12));
      projectList.setToolTipText("All files in this area will have the selected"
                                 + " command applied to them.");
      projectScroller = new JScrollPane ( projectList );
      projectScroller.setPreferredSize(new Dimension (300, 200));
                     
      // add model listeners 
      sourceListModel.addListDataListener(new MyListDataListener());
      projectListModel.addListDataListener(new MyListDataListener());
      
      
      // add list listeners
      sourceList.addListSelectionListener(this);
      projectList.addListSelectionListener(this);  
            
      // add action listeners    
      ffmpegButn.addActionListener(this);
      inputButn.addActionListener(this);
      outputButn.addActionListener(this);
      outputNameChange.addActionListener(this);       
      resetButn.addActionListener(this);     
      saveLogButn.addActionListener(this);
      exitButn.addActionListener(this);
      
      convertButn.addActionListener(this);
      repeatButn.addActionListener(this);
      
      loadFileButn.addActionListener(this);
      fileSelButn.addActionListener(this);
      projectClearButn.addActionListener(this);
      setGlobalButn.addActionListener(this);
      setInputButn.addActionListener(this);
      setOutputButn.addActionListener(this);
      clearButn.addActionListener(this);
      
      
      // Panels
      JPanel topButtonPanel = new JPanel();
      topButtonPanel.add( ffmpegButn );
      topButtonPanel.add( inputButn );
      topButtonPanel.add( outputButn ); 
      topButtonPanel.add( outputNameChange );
      topButtonPanel.add( resetButn );    
      topButtonPanel.add( saveLogButn );      
      topButtonPanel.add( exitButn );   
      
      JPanel bottomButtonPanel = new JPanel();
      bottomButtonPanel.add( loadFileButn );
      bottomButtonPanel.add( fileSelButn );
      bottomButtonPanel.add( projectClearButn );
      bottomButtonPanel.add( setGlobalButn );
      bottomButtonPanel.add( setInputButn );      
      bottomButtonPanel.add( setOutputButn );
      bottomButtonPanel.add( clearButn );  
      
      JPanel sourcePanel = new JPanel();
      sourcePanel.add( sourceScroller );
      
      JPanel projectPanel = new JPanel();
      JLabel projectLabel = new JLabel( "List of files to convert" );
      projectLabel.setSize(300, 10);
      projectPanel.setLayout(null);
      projectPanel.add( projectLabel );
      projectLabel.setBounds( 10, 10, 140, 20);
      projectPanel.add( convertButn );
      convertButn.setBounds(140, 10, 90, 20);
      projectPanel.add( repeatButn );
      repeatButn.setBounds(230, 10, 80, 20);
      projectPanel.add( projectScroller );
      projectScroller.setBounds( 10, 35, 300, 200);
      
      
      // test list now can be done
      sourceListModel.addElement("Please locate ffmpeg.exe to Begin");
 
           
      JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                              sourcePanel, projectPanel);      
 
      JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                              logScrollPane, showCommandText);
      
      notesAndLists = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
      
      add(topButtonPanel, BorderLayout.PAGE_START);
      add(bottomButtonPanel, BorderLayout.PAGE_END);
      add(notesAndLists, BorderLayout.CENTER);
              
   }
   
   /**
    * @Override actionPerformed
    * @param e button pressed 
    */
   public void actionPerformed (ActionEvent e)
   {
       if (e.getSource() == ffmpegButn)
       {
          String response = JOptionPane.showInputDialog(null,
                        "Select the custom delimiter for arguments: ",
                        "ENTER if using the standard \" \" ",
                        JOptionPane.QUESTION_MESSAGE);
          
          // remove extra spaces
          response = response.trim();
          if ( response == null)
          {
             delimString = " ";
          }
          else
          {
             delimString = response;
          }
             // create new FileChooser to search directories and files
             JFileChooser fc = new JFileChooser();
             fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             
             int myAction = fc.showOpenDialog(InputOutput.this);
             
             // if file to be selected is valid
             if (myAction == JFileChooser.APPROVE_OPTION)
             { 
                 // known program location of ffmpeg
                 ffmpegFile = fc.getSelectedFile();
                 ffmpegFileName = ffmpegFile.getName();
                 ffmpegDir = ffmpegFile.getParent();
                 ffmpegButn.setEnabled(false);
                 inputButn.setEnabled(true);
                 outputButn.setEnabled(true);
                 addToLog(true, ( "Executable File: " + ffmpegFileName ));
                 addToLog(false, ( "Executable Path: " + ffmpegDir ));
                 ffmpegDirAndFile = ffmpegFile.getAbsolutePath();
                 
                 // clear source info
                 sourceListModel.clear();
                 
                 // clear project info
                 projectListModel.clear();
             }  
 
       }
       if (e.getSource() == inputButn)
       {
             // create new FileChooser to search directories and files
             JFileChooser fc = new JFileChooser();
             fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             fc.setMultiSelectionEnabled(true);
             
             int myAction = fc.showOpenDialog(InputOutput.this);
             
             // if file to be selected is valid
             if (myAction == JFileChooser.APPROVE_OPTION)
             { 
                int count = 0;
                // source directory for conversions               
                inputDirFile = fc.getSelectedFiles();
                if (inputDirFile.length > 0)
                {
                   count = 0;
                   inputDir = inputDirFile[0].getParent(); 
                   for (int i = 0; i < inputDirFile.length; i++ )
                   {
                      if ( inputDirFile[i].isFile())
                      {   
                          sourceFileSet.add(inputDirFile[i]);
                          count = count +1;
                      }
                   }
                   addToLog(true, ("There are " + count + " files in: "));
                   addToLog(false, ( "Selected Input Path: " + inputDir ));
                   addToLog(false, ("File list: "));
                   for (int i = 0; i < inputDirFile.length; i ++)
                   {
                      if ( inputDirFile[i].isFile())
                      {
                         addToLog(false, inputDirFile[i].getName());
                      }
                   }
                }   
             }
       } 
       if (e.getSource() == outputButn)
       {
             // create new FileChooser to search directories and files
             JFileChooser fc = new JFileChooser();
             fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             fc.setMultiSelectionEnabled(true);
             
             int myAction = fc.showSaveDialog(InputOutput.this);
             
             // if file to be selected is valid
             if (myAction == JFileChooser.APPROVE_OPTION)
             { 
                // source directory for conversions
                outputDirFile = fc.getSelectedFile();
                outputDir = outputDirFile.getParent();
                outputLogName = outputDirFile.getName();              
                addToLog(true, ( "Selected Log File: " + outputLogName ));
                addToLog(false, ( "Selected Output Path: " + outputDir )); 
                loadFileButn.setEnabled(true);
                fileSelButn.setEnabled(true);
                projectClearButn.setEnabled(true);
                setGlobalButn.setEnabled(true);
                setInputButn.setEnabled(true);
                setOutputButn.setEnabled(true);       
             }
       }    
       if (e.getSource() == convertButn)
       {
          String myFile;
          for (int i = 0; i < projectListModel.getSize(); i ++)
          {
               File inFile = (File)projectListModel.get(i);
               myFile = inFile.getName();
               File outFile = new File(outputDir + WINPATHSEP + "conv_" + myFile); 
               addToLog(true, "Conversion: ");
               addToLog(false, "Input File: " + inFile.getAbsolutePath());
               addToLog(false, "Output File: " + outFile.getAbsolutePath());
               executeDirectlyFfmpeg(inFile, outFile);
          }            
       }
       if (e.getSource() == saveLogButn)
       {
           // File aFile = new File(outputDir + WINPATHSEP + "Log " + getDateTime() + ".txt");
           // saveLog(aFile);
          
       } 
       if (e.getSource() == resetButn)
       {
          
       }       
       if (e.getSource() == exitButn)
       {
          System.exit(0);
       }
       if (e.getSource() == loadFileButn)
       {    
          // remove files from the model so no duplicates
          sourceListModel.clear();
          
          // iterator over the set to get all the files
          Iterator sourceIterator = sourceFileSet.iterator();
          while (sourceIterator.hasNext())
          {                  
             sourceListModel.addElement( sourceIterator.next() );
          }// end for 
          
          addToLog(true, ( sourceListModel.getSize() + " Selected source Files."));
          File aFile;
          for (int i = 0; i < sourceListModel.getSize(); i++)
          {
             aFile = (File)sourceListModel.getElementAt(i);
             addToLog(false, aFile.getName());
          }
          clearButn.setEnabled(true);
          projectClearButn.setEnabled(false); //need code
       }
       if (e.getSource() == fileSelButn)
       {       
          convertButn.setEnabled(true);
          repeatButn.setEnabled(true);
          try
          {
            if (!(projectFileSet.contains(sourceFile)))
            {
               projectFileSet.add( sourceFile );
               projectListModel.addElement( sourceFile );
               addToLog(true, "File added to Project List");
               addToLog(false, sourceFile.getParent());
               addToLog(false, sourceFile.getName());             
            }
            else
            {
               addToLog(true, "Not added to Project (Already in Project): " 
                              + sourceFile.getName());
            } 
          }
          catch (NullPointerException npe)
          {
               addToLog(true, "Unable to add file to project: "
                              + "Please select one from the source list."); 
               System.out.println("No such file: " + npe);
          }
          catch (Exception ex)
          {
               System.out.println("An exception fileSelButn " + ex);
          }
       } 
       if (e.getSource() == projectClearButn)
       {
          // code required
       }  
       if (e.getSource() == outputNameChange)
       {
          // for prefix
          // for postfix
          // for file extension change
       }
       if (e.getSource() == setGlobalButn)
       {
          String response = JOptionPane.showInputDialog(null,
                        "Select your global option: ",
                        "ENTER if no selection",
                        JOptionPane.QUESTION_MESSAGE);
          globalRequest = response;
          globalArgList = splitArguments( delimString, response);
 
          showCommandText.setText(setCommand("**FILE IN**", "**FILE OUT**")); 
          addToLog(true, "Selected Global Command: " + response);
 
       }  
       if (e.getSource() == setInputButn)
       {
          String response = JOptionPane.showInputDialog(null,
                        "Select your input option(s): ",
                        "ENTER if no selection",
                        JOptionPane.QUESTION_MESSAGE);
          inputRequest = response; 
          inputArgList = splitArguments( delimString, response);
          showCommandText.setText(setCommand("**FILE IN**", "**FILE OUT**"));  
          addToLog(true, "Selected Input Command: " + response);
          
       }
       if (e.getSource() == setOutputButn)
       {
          String response = JOptionPane.showInputDialog(null,
                        "Select your output option(s): ",
                        "ENTER if no selection",
                        JOptionPane.QUESTION_MESSAGE);
          outputRequest = response;
          outputArgList = splitArguments( delimString, response);          
          showCommandText.setText(setCommand("**FILE IN**", "**FILE OUT**"));
          addToLog(true, "Selected Output Command: " + response);
          
       }         
       if (e.getSource() == clearButn)
       {
          globalRequest = null;
          inputRequest = null;
          outputRequest = null;
          addToLog(true, "Global, input and output commands deleted.");
          showCommandText.setText(setCommand("**FILE IN**", "**FILE OUT**"));          
       }      
       if (e.getSource() == repeatButn)
       {
          // code required
       }          
   } 
 
   /**
    * For Lists
    * @param e 
    */
   @Override
   public void valueChanged(ListSelectionEvent e)
   {
      if ( e.getSource() == sourceList )
      {
         sourceFile = (File)sourceList.getSelectedValue();        
         System.out.println(sourceFile.getName());
      }
      if ( e.getSource() == projectList )
      {
         projectFile = (File)projectList.getSelectedValue();        
         System.out.println(projectFile.getName());                  
      }      
   }
    
    /**
     *  To deal with changes to lists
     */ 
    class MyListDataListener implements ListDataListener 
    {
        public void contentsChanged(ListDataEvent e) 
        {
           System.out.println ("Contents Changed: "
                             + e.getIndex0() + ", " 
                             + e.getIndex1());
        }
        public void intervalAdded(ListDataEvent e) {
           System.out.println ("Interval Added: "
                             + e.getIndex0() + ", " 
                             + e.getIndex1());
        }
        public void intervalRemoved(ListDataEvent e) 
        {
           System.out.println ("Interval Removed: "
                             + e.getIndex0() + ", " 
                             + e.getIndex1());
        }
    }     
   
   /**
    * 
    * @param delimiter the String to split up the argument
    * @param arg the String argument
    * @return an List of each argument delimited in order
    */ 
   private ArrayList<String> splitArguments (String delimiter, String arg)
   {
      ArrayList<String> myList = new ArrayList<String>();
      
      // split up the argument by the custom token
      StringTokenizer specified = new StringTokenizer(delimiter, arg);
      
      // add all the individual elements to the list
      while ( specified.hasMoreTokens())
      {
         myList.add(specified.nextToken());
      }
      
      return myList;     
   }
    
    /**
     * @param inputFilename
     * @param outputFilename
     * @return 
     */
    private String[] setCommandArray (File inputFilename, File outputFilename)
    { 
      String inString = inputFilename.getAbsolutePath();
      String outString = outputFilename.getAbsolutePath();
      
      
      
      String[] commandArray = new String[6];
      commandArray[0] = "ffmpeg ";
      if ( globalRequest != null)
      {
         commandArray[1] = (globalRequest + " "); 
      }
      else
      {
         commandArray[1] = "";
      }
      if ( inputRequest != null)
      {
         commandArray[2] = (inputRequest + " "); 
      }
      else
      {
         commandArray[2] = "";
      } 
      commandArray[3] = "-i ";
      commandArray[4] = inString;
      if ( outputRequest != null)
      {
         commandArray[5] = (outputRequest + " "); 
      }
      else
      {
         commandArray[5] = "";
      }  
      commandArray[6] = outString;// not in use
       
      return commandArray;
    }
    
   /**
    * 
    * @param inputFilename filename of the input (or a designation)
    * @param outputFilename filename of the output (or a designation)
    * @return the command as a text line.
    */
   private String setCommand (String inputFilename, String outputFilename)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("ffmpeg ");
      if ( globalRequest != null)
      {
         sb.append(globalRequest + " "); 
      }
      else
      {
         sb.append("");
      }
      if ( inputRequest != null)
      {
         sb.append(inputRequest + " "); 
      }
      else
      {
         sb.append("");
      }            
      sb.append("-i ");
      sb.append(inputFilename + " ");  // not in use
      if ( outputRequest != null)
      {
         sb.append(outputRequest + " "); 
      }
      else
      {
         sb.append("");
      }                     
      sb.append(outputFilename);  // not in use
      return sb.toString().trim();
   }
   
   /**
    * // for use by:
    *    Specify where ffmpeg is located for the reset of the command
    * 
    * Retrieve the absolute path from ffmpegFile variable
    * @return 
    */
   private String getFfmpegAbsolutePath ()
   {
      if (ffmpegFile.isFile() == true)
      {
         return ffmpegFile.getAbsolutePath();
      }
      return null;
   }
   
   /**
    * 
    * @param argument 
    */
   private void executeDirectlyFfmpeg( File inputFile, File outputFile )
   {
      if ( this.getFfmpegAbsolutePath() == null)
      {
         System.out.println("No specified executable.");
      }
      else
      {
         PumpStreamHandler psh = new PumpStreamHandler();
         try
         {
            HashMap map = new HashMap();           
            map.put("inFile", inputFile.getAbsoluteFile());
            map.put("outFile", outputFile.getAbsoluteFile());            
            
            CommandLine cmdLine = new CommandLine(this.getFfmpegAbsolutePath());
            if ( globalRequest != null)
            {
                Iterator iter = globalArgList.iterator();
                while ( iter.hasNext())
                {
                   cmdLine.addArgument(iter.next().toString());                   
                }
            } 
            if ( inputRequest != null)
            {
                Iterator iter = inputArgList.iterator();
                while ( iter.hasNext())
                {
                   cmdLine.addArgument(iter.next().toString());                   
                }
            }          
            cmdLine.addArgument("-i");
            cmdLine.addArgument("${inFile}");      // from the map
            if ( outputRequest != null)
            {
                Iterator iter = outputArgList.iterator();
                while ( iter.hasNext())
                {
                   cmdLine.addArgument(iter.next().toString());                   
                }
            }            
            cmdLine.addArgument("${outFile}");     // from the map      
            cmdLine.setSubstitutionMap(map);
 
            
            // handles execution
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            Executor exec = new DefaultExecutor();
            // kills a process lasting loger than 10 minutes            
            ExecuteWatchdog woof = new ExecuteWatchdog(600*1000);
            exec.setWatchdog(woof); 
            exec.execute(cmdLine, resultHandler);
            
            
            // wait a little for the result to happen
            resultHandler.waitFor();
         }
         catch (ExecuteException ee)
         {
            // do something
         }
         catch (InterruptedException ie)
         {
            // do something else
         }
         catch (IOException ioe)
         {
            ioe.printStackTrace();
         }
      }
   }
   
   /**
    * @param commandString command to be applied to ffmpeg
    */
   private void executeFFmpegCommand(String commandString)
   {
      StringBuilder sb = new StringBuilder();
      
      // specifies the executive ffmpeg.exe
      sb.append(ffmpegDirAndFile);
      sb.append(" ");
      
      // strips out the string "ffmpeg " - but this may be required!!
      // sb.append(commandString.substring(8));
      try
      {   
         Process p = Runtime.getRuntime().exec(sb.toString());
         InputStreamReader inSr = new InputStreamReader( p.getInputStream() );
         InputStreamReader erSr = new InputStreamReader( p.getErrorStream() );
         BufferedReader inRead = new BufferedReader( inSr );
         BufferedReader erRead = new BufferedReader( erSr );
         p.getInputStream().close();
         getAllStreamText( p.getErrorStream() );
         getAllStreamText( p.getInputStream() );
      }
      catch (IOException ioe)
      {
         System.out.println("An Exception: " + ioe);
      }
   }
   
   /**
    * 
    * @param inStream
    * @return 
    */
   private String getAllStreamText ( InputStream inStream )
   {
      StringBuilder sb = new StringBuilder();
      try
      {
         InputStreamReader inRead = new InputStreamReader ( inStream );
         BufferedReader bufRead = new BufferedReader ( inRead );
         String eachLine = bufRead.readLine();
         while ( eachLine != null)
         {
            sb.append( eachLine );
            eachLine = bufRead.readLine();
         }
      }
      catch ( IOException ioe )
      {
         System.out.println("An Exception: " + ioe);
      }
      
      return sb.toString();
   }
   
   /**
    * Used to send a string and stamp it with time and date
    * @param timeDate if true, time and date stamped
    * @param aString the text sent to the log file
    */
   private void addToLog( Boolean timeDate, String sentence )
   {
      // if send time and date to log
      if ( timeDate )
      {
         log.append( getDateTime() + " " + sentence);
      }
      else
      {
         log.append( INDENT + sentence);
      }
      log.append( NEWLINE );
   log.setCaretPosition(log.getDocument().getLength());    
   }
   
   /**
    * to write out the log file
    */
   private void saveLog (File aFile)
   {
      try
      {   
         if (!aFile.exists())
         {
            // make a new log file
            aFile.createNewFile();
         }
         FileWriter fw = new FileWriter(aFile.getAbsoluteFile());
         BufferedWriter writeLog = new BufferedWriter(fw);
         
         String myString = log.toString();        
         writeLog.write(myString);
         writeLog.close();
      }
      catch (IOException ioe)
      {
         System.out.println("An Exception " + ioe);
      }
   }
   
   /**
    * 
    * @return current data and time as a string
    */
   private String getDateTime() 
   {
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      Date date = new Date();
      return ("[ " + dateFormat.format(date) + "]:  ");
   } 
   
   /**
    * To avoid problems with multiple threads and
    * create and show this GUI
    */
    private static void displayGraphicInterface()
    {
         JFrame myFrame = new JFrame("FFmpeg Convertor");
         
         // close on exit
         myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          
         JComponent newContent = new InputOutput();
         newContent.setOpaque(true);
         myFrame.setContentPane(newContent);
          
         // make sure it fills the frame
         myFrame.pack();
         
         // make sure the frame is visible!
         myFrame.setVisible(true);
    }
     
    /**
     * 
     * Class is executable
     */
     public static void main(String[] args)
     {
        SwingUtilities.invokeLater(new Runnable()
                // open invoke later argument
            {
                public void run()
                {
                    // run the private method to display interface
                    displayGraphicInterface();
                }    
            } 
        );      // close the invoke later argument  
    } 
}
