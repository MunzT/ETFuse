package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class SyncWizardHelpFrame extends JDialog {
	
	public SyncWizardHelpFrame(SyncWizardFrame parentFrame) {
		
		super(parentFrame, "Instruktionen", true);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JTextArea txtrInstructions = new JTextArea();
		txtrInstructions.setWrapStyleWord(true);
		txtrInstructions.setEditable(false);
		txtrInstructions.setLineWrap(true);
		txtrInstructions.setText("Schritt 1 \u2013 Laden des Gast-Datensatzes.\r\nIm ersten Schritt wird der Datensatz des Gast-Recordings geladen.\r\nDieser befindet sich im TSV-Format.\r\n\r\nSchritt 2 \u2013 Festlegen des Spielfeld-Bereiches im Gast-Recording.\r\nIm zweiten Schritt wird der Bereich des Videos im Gast-Recording ausgew\u00E4hlt, der von der Koordinatentransformation betroffen sein soll. Hierf\u00FCr stehen dem Nutzer zwei Optionen zur Wahl, Presets oder eine manuelle Bestimmung. Unter den Presets gibt es zwei vorgefertigte Bereiche, die auf die Recordings passen, die im Rahmen dieser Arbeit mit den zwei Ger\u00E4ten get\u00E4tigt wurden. Alternativ kann der Bereich jedoch auch manuell festgelegt werden. Hierf\u00FCr ist die Bildschirmaufzeichnungdes Gast-Recordings notwendig. Wird diese Option gew\u00E4hlt, \u00F6ffnet sich ein Dateiauswahldialog, \u00FCber den das Video geladen werden kann. W\u00E4hlt der Nutzer eine g\u00FCltige Datei aus, \u00F6ffnet sich einweiteres Fenster mit dem Video des Gast-Recordings, in dem mittels Mausklick die obere linke und die untere rechte Ecke des Spielfeldes markiert werden kann. Erfolgt dies, geht der Wizard in den n\u00E4chsten Schritt \u00FCber.\r\n\r\nSchritt 3 \u2013 Festlegen des Spielfeld-Bereiches im Host-Recording.\r\nAnalog zu Schritt 2 wird im dritten Schritt der Spielfeld-Bereich im Host-Recording bestimmt. Auch hierf\u00FCr stehen dieselben Presets wie aus dem vorherigen Schritt bereit. Alternativ kann auch hier der Bereich manuell festgelegt werden. Ein Laden einer Video-Datei ist in diesem Schritt jedoch nicht n\u00F6tig, da das Host-Recording bereits geladen ist und vom Wizard ber\u00FCcksichtigt wird. Ist dieser Schritt erfolgt, geht der Wizard in den letzten Schritt \u00FCber.\r\n\r\nSchritt 4 \u2013 Wahl der Synchronisations-Methode.\r\nIm letzten Schritt w\u00E4hlt der Nutzer zwischen drei Optionen zur zeitlichen Synchronisation der Recordings. Zur Auswahl stehen eine ungenaue, schnelle Methode, die sich nur an den System-Zeitstempeln in den Recordings orientiert, und zwei pr\u00E4zisere Methoden. Wird die ungenaue Methode gew\u00E4hlt, ist die Konfiguration abgeschlossen und das Gast-Recording wird hinzugef\u00FCgt. Wird hingegen eine der anderen beiden Methoden gew\u00E4hlt, sind zus\u00E4tzliche Angaben notwendig, die dem jeweiligen Verfahren als Richtwerte dienen, und das Screen-Recording zum Gastdatensatz wird ben\u00F6tigt. F\u00FCr die Synchronisation via spezifischer (experimenteller) Methode muss die Position des Steines auf dem Spielfeld bestimmt werden, an dem sich das Programm orientieren soll. F\u00FCr die Synchronisation via Histogramm-Vergleich wird der Nutzer gebeten, ein Start-Frame aus dem Host-Screen-Recording auszuw\u00E4hlen, das zwischen zwei Z\u00FCgen im Spiel liegt. Zum T\u00E4tigen dieser Angaben \u00F6ffnet sich jeweils das Host-Screen-Recording in einem separaten Fenster. Sobald die Angaben get\u00E4tigt wurden, ist der Wizard bereit, das Gast-Recording hinzuzuf\u00FCgen.");
		getContentPane().add(txtrInstructions, BorderLayout.CENTER);
		
		this.setMinimumSize(new Dimension(550, 700));
		
		this.pack();
		
		setLocationRelativeTo(null);
	}
}
