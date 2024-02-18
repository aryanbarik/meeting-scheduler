package main.csp;

import java.time.LocalDate;
import java.util.*;

/**
 * CSP: Calendar Satisfaction Problem Solver
 * Provides a solution for scheduling some n meetings in a given
 * period of time and according to some unary and binary constraints
 * on the dates of each meeting.
 */
public class CSPSolver {

    // Backtracking CSP Solver
    // --------------------------------------------------------------------------------------------------------------
    
    /**
     * Public interface for the CSP solver in which the number of meetings,
     * range of allowable dates for each meeting, and constraints on meeting
     * times are specified.
     * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The start date (inclusive) of the domains of each of the n meeting-variables
     * @param rangeEnd The end date (inclusive) of the domains of each of the n meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary for this assignment)
     * @return A list of dates that satisfies each of the constraints for each of the n meetings,
     *         indexed by the variable they satisfy, or null if no solution exists.
     */
    public static List<LocalDate> solve (int nMeetings, LocalDate rangeStart, LocalDate rangeEnd, Set<DateConstraint> constraints) {
    	List<LocalDate> localDateList = new ArrayList<LocalDate>();
    	List<MeetingDomain> meetingDomainList = getDomainList(nMeetings, rangeStart, rangeEnd);
    	
    	return recursiveBacktracking(localDateList, meetingDomainList, nMeetings, rangeStart, rangeEnd, constraints);
    }
    
    /**
     * Helper method which creates a list of MeetingDomains which will record keep the domain for each meeting
     * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The LocalDate the list should start at
     * @param rangeEnd The LocalDate the list should end at
     * @return A list of meeting domains where the domain at each index describes the domain of a local date at a corresponding index in another list
     */
    private static List<MeetingDomain> getDomainList (int nMeetings, LocalDate rangeStart, LocalDate rangeEnd) {
    	List<MeetingDomain> meetingDomainList = new ArrayList<MeetingDomain>();
    	MeetingDomain currentMeetingDomain = new MeetingDomain(rangeStart, rangeEnd);
    	
    	for (int i = 0; i < nMeetings; i++) {
    		MeetingDomain nextMeedingDomain = new MeetingDomain(currentMeetingDomain);
    		meetingDomainList.add(nextMeedingDomain);
    	}
    	
    	return meetingDomainList;
    }
    
	/**
     * Recursive helper method for solve
 	 * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The start date (inclusive) of the domains of each of the n meeting-variables
     * @param rangeEnd The end date (inclusive) of the domains of each of the n meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary for this assignment)
     * @return The list of dates found which fit the constraints so far
     */
    private static List<LocalDate> recursiveBacktracking (List<LocalDate> assignment, List<MeetingDomain> meetingDomainList, int nMeetings, LocalDate rangeStart, LocalDate rangeEnd, Set<DateConstraint> constraints) {
    	List<LocalDate> result = new ArrayList<LocalDate>();
    	
    	// base case
    	if (assignment.size() == nMeetings)
    		return assignment;
    	
    	MeetingDomain currentDomain = meetingDomainList.get(assignment.size());
    	
    	for (LocalDate possibleDate : currentDomain.domainValues) {
    		assignment.add(possibleDate);
    		
    		if (meetsConstraints(assignment, constraints)) {
    			result = recursiveBacktracking(assignment, meetingDomainList, nMeetings, rangeStart, rangeEnd, constraints);
    		
				if (result != null) {
					return result;
				}
    		}
    		

			assignment.remove(assignment.size() - 1);

    	}
    	return null;
    }
    
   /**
    * Method which checks if current assignments meets the set of constraints
    * @param assignment List of LocalDates assigned
    * @param constraints Set of constraints which assignment needs to satisfy
    * @return true if assignment meets constraints, false otherwise
    */
    private static boolean meetsConstraints (List<LocalDate> assignment, Set<DateConstraint> constraints) {
		for (DateConstraint currentConstraint : constraints) {

			if (currentConstraint.arity() == 1) {
				UnaryDateConstraint currentUnaryConstraint = (UnaryDateConstraint) currentConstraint;

				if (currentUnaryConstraint.L_VAL >= assignment.size())
					continue;
				
				if (!unaryConstraintSatisfied(currentUnaryConstraint, assignment)) 
					return false;

			} else {
				BinaryDateConstraint currentBinaryConstraint = (BinaryDateConstraint) currentConstraint;
				
				if (currentBinaryConstraint.L_VAL >= assignment.size() || currentBinaryConstraint.R_VAL >= assignment.size() ) {
					continue;
				}
				if (!binaryConstraintSatisfied(currentBinaryConstraint, assignment))
					return false;
			}
		}
		
		return true;
    }
    
    
    /**
     * Method which returns true if a list of LocalDates satisfied the binary constraint, false if not
     * @param binaryConstraint The binary constraint to check  
     * @param assignment The list to check the constraint against
     * @return true if a list of LocalDates satisfied the unary constraint, false if not
     */
    private static boolean binaryConstraintSatisfied (BinaryDateConstraint binaryConstraint, List<LocalDate> assignment) {
    	int leftIndexToCheck = binaryConstraint.L_VAL;
    	int rightIndexToCheck = binaryConstraint.R_VAL;
    	
    	if (binaryConstraint.isSatisfiedBy(assignment.get(leftIndexToCheck), assignment.get(rightIndexToCheck)))
    		return true;
    	
    	return false;
    }
    
    /**
     * Method which returns true if a list of LocalDates satisfies the unary constraint, false if not
     * @param unaryConstraint The unary constraint to check 
     * @param assignment The list to check the constraint against
     * @return true if a list of LocalDates satisfied the binary constraint, false if not
     */
    private static boolean unaryConstraintSatisfied (UnaryDateConstraint unaryConstraint, List<LocalDate> assignment) {
    	int listIndexToCheck = unaryConstraint.L_VAL;
    	LocalDate dateToCheck = unaryConstraint.R_VAL;
    	
    	if (unaryConstraint.isSatisfiedBy(assignment.get(listIndexToCheck), dateToCheck))
    		return true;
    	
    	return false;
    } 
    
    
    // Filtering Operations
    // --------------------------------------------------------------------------------------------------------------
    
    /**
     * Enforces node consistency for all variables' domains given in varDomains based on
     * the given constraints. Meetings' domains correspond to their index in the varDomains List.
     * @param varDomains List of MeetingDomains in which index i corresponds to D_i
     * @param constraints Set of DateConstraints specifying how the domains should be constrained.
     * [!] Note, these may be either unary or binary constraints, but this method should only process
     *     the *unary* constraints! 
     */
    public static void nodeConsistency (List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
    	Set<UnaryDateConstraint> unaryConstraints = getUnaryConstraints(constraints);

		for (UnaryDateConstraint currentConstraint : unaryConstraints) {
			MeetingDomain currentDomain = varDomains.get(currentConstraint.L_VAL);
			Set<LocalDate> replacementDomain = new HashSet<LocalDate>();
			
			for (LocalDate date : currentDomain.domainValues) {
				
				if (currentConstraint.isSatisfiedBy(date, currentConstraint.R_VAL)) {
					replacementDomain.add(date);
				}
			}			
			
			currentDomain.domainValues = replacementDomain;
		}
    }
    
    /**
     * Takes a set of constraints and returns a the unary constraints contained as a set of UnaryDateConstraints
     * @param constraints Set of DateConstraints to collect unary constraints from
     * @return Set of UnaryDateConstraint of the unary constraints contained in constraints
     */
    private static Set<UnaryDateConstraint> getUnaryConstraints (Set<DateConstraint> constraints) {
    	Set<UnaryDateConstraint> unaryConstraints = new HashSet<UnaryDateConstraint>();
    	
    	for (DateConstraint constraint : constraints) {
    		if (constraint.arity() == 1) {
    			UnaryDateConstraint currentUnaryConstraint = (UnaryDateConstraint) constraint;
    			unaryConstraints.add(currentUnaryConstraint);
    			
    		}
    	}
    	
    	return unaryConstraints;
    }
        
    /**
     * Enforces arc consistency for all variables' domains given in varDomains based on
     * the given constraints. Meetings' domains correspond to their index in the varDomains List.
     * @param varDomains List of MeetingDomains in which index i corresponds to D_i
     * @param constraints Set of DateConstraints specifying how the domains should be constrained.
     * [!] Note, these may be either unary or binary constraints, but this method should only process
     *     the *binary* constraints using the AC-3 algorithm! 
     */
    public static void arcConsistency (List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
    	Set<BinaryDateConstraint> binaryConstraints = getBinaryConstraints(constraints);
    	
//    	Pseudo:
//    		inputs: csp with variables
//    		local variables: set, set of arcs, initially all sets in CSP
//		
    	
    	HashSet<Arc> currentArcSet = new HashSet<Arc>();
    	HashSet<Arc> fullArcSet = new HashSet<Arc>();
    	
    	for (BinaryDateConstraint constraint : binaryConstraints) {
    		currentArcSet.addAll(getArcSet(constraint));
    		fullArcSet.addAll(getArcSet(constraint));
    	}
    		
//      while set is not empty:
    	while (!currentArcSet.isEmpty()) {
    		
////		remove first item in set (Xi, Xj)
    		// Tail: Xi, head: Xj
    		Iterator<Arc> itr = currentArcSet.iterator();
    		Arc currentArc = itr.next();
    		currentArcSet.remove(currentArc);
    		
//			if remove-inconsistent values(Xi, Xj) then
    		if (removeInconsistentValues(currentArc, varDomains)) {
//    			for each Xk in neighbors[Xi]    			
    			for (Arc neighborArc : fullArcSet) {
    				if (currentArc.TAIL == neighborArc.HEAD) {
//    					add (Xk, Xi) to set
    					currentArcSet.add(neighborArc);
    				}
    			}

			}

		}
    }
    
    
    private static boolean removeInconsistentValues(Arc currentArc, List<MeetingDomain> varDomains) {
//		function remove-inconsistent values (Xi, Xj) returns if succeeds
//		removed <-- false
    	
    	
    	Set<LocalDate> tailDomain = varDomains.get(currentArc.TAIL).domainValues, 
    			headDomain = varDomains.get(currentArc.HEAD).domainValues,
    			newTailDomain = new HashSet<LocalDate>();
    	
////    	uncomment from here
////		for each x in Domain[Xi]
//    	for (LocalDate tailDate : tailDomain) {
//    		for (LocalDate headDate : headDomain) {
////    			if no value y in Domain[Xj] allows (x,y) to satisfy the constraint Xi <--> Xj
//    			if (currentArc.CONSTRAINT.isSatisfiedBy(tailDate, headDate)) {
////					then delete x from Domain[Xi]; removed <--- true
//    				break;
//    			}
//    		}
//    		
//        	tailDomain = newTailDomain;
//    	}

//    	uncomment from here
    	Iterator<LocalDate> tailIter = tailDomain.iterator();
    	Iterator<LocalDate> headIter = headDomain.iterator();
    	
    	LocalDate tailDate = tailIter.next();
    	LocalDate headDate = headIter.next();
    	
//		for each x in Domain[Xi]
    	while (tailIter.hasNext()) {
    		while (headIter.hasNext()) {
//    			if no value y in Domain[Xj] allows (x,y) to satisfy the constraint Xi <--> Xj
    			
    	    	if (currentArc.CONSTRAINT.isSatisfiedBy(tailDate, headDate)) {
    	    		newTailDomain.add(tailDate);
    	    	}
    			
//				then delete x from Domain[Xi]; removed <--- true  
    			headIter.next();
    		}
    		
    	}
    	
		return !(tailDomain.equals(newTailDomain));
	}
    

	/**
     * Returns set of arcs from a constraint
     * @param constraint to generate arc set from
     * @return a set of arcs for associated with a constraint
     */
    private static Set<Arc> getArcSet (BinaryDateConstraint constraint) {
    	HashSet<Arc> arcSet = new HashSet<Arc>();
    	
		Arc arc1 = new Arc(constraint.L_VAL, constraint.R_VAL, constraint), arc2 = new Arc(constraint.R_VAL, constraint.L_VAL, constraint);
		arcSet.add(arc1);
		arcSet.add(arc2);
    	
    	return arcSet;
    }
    
    
    		
    		
    		/**
    		 * AC-3 Procedure
    		 * 1
    		 * - Maintain a set of arcs (tail--> head)
    		 * - start w/ ALL arcs from the constraint graph
    		 * 
    		 * 2
    		 * - while set is not empty
    		 * - pop an arc
    		 * - if consistent, continue
    		 * - else
    		 * - make tail domain consistent
    		 * - re-add all neighboring arcs that point to that tail (propagation)
    		 * 
    		 */
				

    
    
    /**
     * Takes a set of constraints and returns a the binary constraints contained as a set of BinaryDateConstraints
     * @param constraints Set of DateConstraints to collect binary constraints from
     * @return Set of BinaryDateConstraint of the binary constraints contained in constraints
     */
    private static Set<BinaryDateConstraint> getBinaryConstraints (Set<DateConstraint> constraints) {
    	Set<BinaryDateConstraint> binaryConstraints = new HashSet<BinaryDateConstraint>();
    	
    	for (DateConstraint constraint : constraints) {
    		if (constraint.arity() == 2) {
    			BinaryDateConstraint currentBinaryConstraint = (BinaryDateConstraint) constraint;
    			binaryConstraints.add(currentBinaryConstraint);
    			
    		}
    	}
    	
    	return binaryConstraints;
    }
    
    /**
     * Private helper class organizing Arcs as defined by the AC-3 algorithm, useful for implementing the
     * arcConsistency method.
     * [!] You may modify this class however you'd like, its basis is just a suggestion that will indeed work.
     */
    private static class Arc {
        
        public final DateConstraint CONSTRAINT;
        public final int TAIL, HEAD;
        
        /**
         * Constructs a new Arc (tail -> head) where head and tail are the meeting indexes
         * corresponding with Meeting variables and their associated domains.
         * @param tail Meeting index of the tail
         * @param head Meeting index of the head
         * @param c Constraint represented by this Arc.
         * [!] WARNING: A DateConstraint's isSatisfiedBy method is parameterized as:
         * isSatisfiedBy (LocalDate leftDate, LocalDate rightDate), meaning L_VAL for the first
         * parameter and R_VAL for the second. Be careful with this when creating Arcs that reverse
         * direction. You may find the BinaryDateConstraint's getReverse method useful here.
         */
        public Arc (int tail, int head, DateConstraint c) {
            this.TAIL = tail;
            this.HEAD = head;
            this.CONSTRAINT = c;
        }
        
        @Override
        public boolean equals (Object other) {
            if (this == other) { return true; }
            if (this.getClass() != other.getClass()) { return false; }
            Arc otherArc = (Arc) other;
            return this.TAIL == otherArc.TAIL && this.HEAD == otherArc.HEAD && this.CONSTRAINT.equals(otherArc.CONSTRAINT);
        }
        
        @Override
        public int hashCode () {
            return Objects.hash(this.TAIL, this.HEAD, this.CONSTRAINT);
        }
        
        @Override
        public String toString () {
            return "(" + this.TAIL + " -> " + this.HEAD + ")";
        }
        
    }
    
}
