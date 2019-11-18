package de.uni_stuttgart.visus.etfuse.eyetracker.gazefilter;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;

public class IVTFilter {
	
	private static class PointToPointVelocity {
		int idx;
		double velocity;
		Boolean isFixation = false;
	}
	
	private static class Fixation {
		int startIdx;
		int stopIdx;
		double x;
		double y;
	}
	
	public static EyeTrackerRecording filterRecording(EyeTrackerRecording rec, double velocityThreshold, int distanceThreshold) {
				
		ArrayList<EyeTrackerEyeEvent> events = rec.getRawEyeEvents();
				
		ArrayList<PointToPointVelocity> p2pvs = new ArrayList<PointToPointVelocity>();
		
		PointToPointVelocity first = new PointToPointVelocity();
		first.idx = 0;
		first.velocity = 0.0;
		p2pvs.add(first);
				
		for (int i = 1; i < events.size() - 1; i++) {
			
			EyeTrackerEyeEvent e0 = events.get(i - 1);
			EyeTrackerEyeEvent e1 = events.get(i);
			EyeTrackerEyeEvent e2 = events.get(i + 1);
			
			double angleVelocitye0e1 = calculateInterPointVelocity(e0, e1, rec.getDisplayPPI(), rec.getSamplingFrequency());
			double angleVelocitye1e2 = calculateInterPointVelocity(e1, e2, rec.getDisplayPPI(), rec.getSamplingFrequency());
			
			double pointToPointVelocity = (angleVelocitye0e1 + angleVelocitye1e2) / 2.0;
			
			PointToPointVelocity p2pv = new PointToPointVelocity();
			p2pv.idx = i;
			p2pv.velocity = pointToPointVelocity;
			p2pv.isFixation = (pointToPointVelocity < velocityThreshold ? true : false);
			p2pvs.add(p2pv);
		}
		
		PointToPointVelocity last = new PointToPointVelocity();
		last.idx = events.size() - 1;
		last.velocity = 0.0;
		p2pvs.add(last);
		
		ArrayList<EyeTrackerEyeEvent> fixations = generateFixationEvents(events, filterByDistanceThreshold(mapFixationGroupsToCentroid(generateFixationGroups(events, p2pvs)), distanceThreshold));
		
		rec.setFilteredEyeEvents(fixations);
		
		System.out.println("<IVTFilter> Events: " + events.size() + " Fixations: " + fixations.size());
		
		return rec;
	}
	
	private static double calculateEventEyeDistAverage(EyeTrackerEyeEvent e) {
		
		double eyePosAverageX = (e.eyePosLeftX + e.eyePosRightX) * 0.5;
		double eyePosAverageY = (e.eyePosLeftY + e.eyePosRightY) * 0.5;
		double observerBaseDist = (e.eyePosLeftZ + e.eyePosRightZ) * 0.5;
		
		Point2D eventPoint = new Point2D.Double(e.realFixationPointX, e.realFixationPointY);
		Point2D basePoint = new Point2D.Double(eyePosAverageX, eyePosAverageY);
		
		double eventBaseDist = eventPoint.distance(basePoint);
		
		double eyeEventDist = Math.sqrt(Math.pow(observerBaseDist, 2) + Math.pow(eventBaseDist, 2) - 2 * observerBaseDist * eventBaseDist * Math.cos(Math.toRadians(90)));
		
		return eyeEventDist;
	}
	
	private static double calculateInterPointVelocity(EyeTrackerEyeEvent e1, EyeTrackerEyeEvent e2, double displayPPI, double samplingFrequency) {
		
		double averageEyeDist1_b = IVTFilter.calculateEventEyeDistAverage(e1);
		double averageEyeDist2_a = IVTFilter.calculateEventEyeDistAverage(e2);
		double averageEyeDistBetweenEvents = 0.5 * (averageEyeDist1_b + averageEyeDist2_a);

		Point2D e1P_A = new Point2D.Double(e1.realFixationPointX, e1.realFixationPointY);
		Point2D e2P_B = new Point2D.Double(e2.realFixationPointX, e2.realFixationPointY);
		
		double eventPointDist_c = e1P_A.distance(e2P_B);
		double eventPointDist_mm = pixelDistanceToMillimeters(eventPointDist_c, displayPPI);
		
		double gazeAngle_gamma = Math.acos((Math.pow(eventPointDist_mm, 2) - Math.pow(averageEyeDistBetweenEvents, 2) - Math.pow(averageEyeDistBetweenEvents, 2)) / (-2 * averageEyeDistBetweenEvents * averageEyeDistBetweenEvents));

		gazeAngle_gamma = Math.toDegrees(gazeAngle_gamma);
		
		if (Double.isNaN(gazeAngle_gamma)) {
			System.out.println("NaN :(");
			System.out.println("b: " + averageEyeDist1_b + " a: " + averageEyeDist2_a + " c: " + eventPointDist_c + " e1: " + e1P_A + " e2: " + e2P_B);
		}
		
		double eventAngleSeconds = gazeAngle_gamma / (1.0 / samplingFrequency);
		
		return eventAngleSeconds;
	}
	
	private static ArrayList<EyeTrackerEyeEvent> filterOutEventsWithoutEyes(ArrayList<EyeTrackerEyeEvent> events) {
		
		ArrayList<EyeTrackerEyeEvent> filtered = new ArrayList<EyeTrackerEyeEvent>();
		
		for (EyeTrackerEyeEvent e : events) {
			
			if (e.eyesNotFound)
				continue;
			
			filtered.add(e);
		}
		
		return filtered;
	}
	
	private static double pixelDistanceToMillimeters(double distance, double ppi) {
		
		double inches = distance / ppi;
		double mm = inches / 0.039370;
		return mm;
	}
	
	private static ArrayList<ArrayList<Fixation>> generateFixationGroups(ArrayList<EyeTrackerEyeEvent> events, ArrayList<PointToPointVelocity> protocol) {
		
		ArrayList<ArrayList<Fixation>> fixationGroups = new ArrayList<ArrayList<Fixation>>();
		ArrayList<Fixation> currentFixationGroup = new ArrayList<Fixation>();
		
		for (PointToPointVelocity p : protocol) {
			
			if (p.isFixation) {
				Fixation fix = new Fixation();
				fix.startIdx = p.idx;
				fix.stopIdx = p.idx;
				fix.x = events.get(p.idx).realFixationPointX;
				fix.y = events.get(p.idx).realFixationPointY;
				currentFixationGroup.add(fix);
			}
			else {
				if (currentFixationGroup.size() > 0) {
					fixationGroups.add(currentFixationGroup);
					currentFixationGroup = new ArrayList<Fixation>();
				}
			}
		}
		
		return fixationGroups;		
	}
	
	private static ArrayList<Fixation> mapFixationGroupsToCentroid(ArrayList<ArrayList<Fixation>> fixationGroups) {
		
		ArrayList<Fixation> fixations = new ArrayList<Fixation>();
		
		for (ArrayList<Fixation> currentGroup : fixationGroups) {
			
			double fixPointX = 0.0;
			double fixPointY = 0.0;
			
			for (Fixation f : currentGroup) {
				fixPointX += f.x;
				fixPointY += f.y;
			}
			
			fixPointX = fixPointX / currentGroup.size();
			fixPointY = fixPointY / currentGroup.size();
			
			Fixation newF = new Fixation();
			newF.x = fixPointX;
			newF.y = fixPointY;
			newF.startIdx = currentGroup.get(0).startIdx;
			newF.stopIdx = currentGroup.get(currentGroup.size() - 1).stopIdx;
			
			fixations.add(newF);
		}
		
		return fixations;
	}
	
	private static ArrayList<Fixation> filterByDistanceThreshold(ArrayList<Fixation> fixations, int distanceThreshold) {
		
		ArrayList<Fixation> filteredFixations = fixations;
		ArrayList<Fixation> newFixations = new ArrayList<Fixation>();
		
		Boolean repeat = true;
		
		while (repeat) {
			
			repeat = false;
			
			for (int i = 0; i < filteredFixations.size() - 1; i++) {
				
				Fixation e1 = filteredFixations.get(i);
				Fixation e2 = filteredFixations.get(i + 1);
				
				Point2D e1P = new Point2D.Double(e1.x, e1.y);
				Point2D e2P = new Point2D.Double(e2.x, e2.y);
				
				if (e1P.distance(e2P) < distanceThreshold) {
										
					repeat = true;
					
					double newX = (e1.x + e2.x) / 2.0;
					double newY = (e1.y + e2.y) / 2.0;
					
					Fixation fNew = new Fixation();
					fNew.x = newX;
					fNew.y = newY;
					fNew.startIdx = e1.startIdx;
					fNew.stopIdx = e2.stopIdx;
					newFixations.add(fNew);
					i++;
				}
				else {
					newFixations.add(e1);
				}
			}

			filteredFixations = newFixations;

			if (repeat)
				newFixations = new ArrayList<Fixation>();	
		}
		
		return filteredFixations;
	}
	
	private static ArrayList<EyeTrackerEyeEvent> generateFixationEvents(ArrayList<EyeTrackerEyeEvent> allEvents, ArrayList<Fixation> fixations) {
		
		ArrayList<EyeTrackerEyeEvent> fixationEvents = new ArrayList<EyeTrackerEyeEvent>();
		
		for (Fixation f : fixations) {
			
			EyeTrackerEyeEvent startingEvent = allEvents.get(f.startIdx);
			
			EyeTrackerEyeEvent fixE = new EyeTrackerEyeEvent();
			fixE.fixationPointX = (int) Math.round(f.x);
			fixE.fixationPointY = (int) Math.round(f.y);
			fixE.timestamp = startingEvent.timestamp;
			fixE.number = startingEvent.number;
			fixE.eyesNotFound = startingEvent.eyesNotFound;
			fixE.fixationDuration = allEvents.get(f.stopIdx).timestamp - fixE.timestamp;
			
			fixationEvents.add(fixE);
		}
		
		return fixationEvents;
	}
}
