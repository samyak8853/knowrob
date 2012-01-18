package edu.tum.cs.vis.action;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.vecmath.Vector2f;

import edu.tum.cs.vis.gui.applet.PlanVisApplet;

import processing.core.PApplet;

/**
 * This class is used in an Action class for saving parameters used during draw of the action.
 * For example this stores the position/size of the action box.
 * 
 * There are two different drawing types:
 *  - Simple box: Contains only title and properties
 *  - Extended box: Parents and children of action, properties and subsequences.
 * 
 * @author Stefan Profanter
 * @see edu.tum.cs.vis.action.Action
 */
public class ActionDrawInformation {
	
	/**
	 * Text line height
	 */
	public final static float LINE_HEIGHT = 1.5f;
	
	/**
	 * Padding between sections of the action.
	 * E.g. between properties and subsequence box 
	 */
	public final static float INNER_CONTENT_PADDING = 10f;
	
	/**
	 * Padding between main box border and content
	 */
	public final static float MAIN_BOX_PADDING = 15f;
	
	/**
	 * Padding around a single sequence box in the subsequence box
	 */
	public final static float SEQUENCE_BOX_PADDING = 10f;
	
	/**
	 * total height of the expand box under a subsequence box
	 */
	public final static float EXPAND_BOX_HEIGHT = 18f;
		
	/**
	 * Default border color for boxes
	 */
	private final static Color borderColor = new Color(100, 100, 100);
	/**
	 * Default background color for boxes
	 */
	private final static Color backgroundColor = new Color(30,30,30);
	/**
	 * Default background color for subsequence box
	 */
	private final static Color backgroundBrightColor = new Color(50,50,50);
	/**
	 * Default text color
	 */
	private final static Color textColor = new Color(240,240,240);
	
	/**
	 * border color for boxes when hovering
	 */
	private final static Color hoverBorderColor = new Color(200, 200, 200);
	/**
	 * background color for boxes when hovering
	 */
	private final static Color hoverBackgroundColor = new Color(60,60,60);
	/**
	 * background color for subsequence box when hovering
	 */
	private final static Color hoverBackgroundBrightColor = new Color(100,100,100);
	/**
	 * text color when hovering
	 */
	private final static Color hoverTextColor = new Color(255,255,255);
	
	/**
	 * Border color of arrow between sequence boxes
	 */
	private final static Color arrowBorderColor = new Color(27, 56, 102);
	/**
	 * Background color of arrow between sequence boxes
	 */
	private final static Color arrowBackgroundColor = new Color(51,105,192,230);
	

	/**
	 * Border color of arrow for expanding box
	 */
	private final static Color arrowExpandBorderColor = new Color(94, 61, 61);
	/**
	 * Background color of arrow expanding box
	 */
	private final static Color arrowExpandBackgroundColor = new Color(125,67,67,230);	
	/**
	 * Border color of arrow for expanding box
	 */
	private final static Color hoverArrowExpandBorderColor = new Color(190, 123, 123);
	/**
	 * Background color of arrow expanding box
	 */
	private final static Color hoverArrowExpandBackgroundColor = new Color(190,141,141,230);
	
	/**
	 * Default line/border width
	 */
	private final static float defaultStroke = 1f;
	/**
	 * line/border width of current action 
	 */
	private final static float currentStroke = 1.5f;
	
	/**
	 * Used for intern calculation such as if mouse is hovering.
	 * Indicates if drawSimpleBox or drawExtendedBox was used to draw this action.
	 */
	private boolean drawnAsSimple;
	
	/**
	 * Following colors are set to default colors if not hovering
	 */
	private Color currentBorderColor = borderColor;
	private Color currentBackgroundColor = backgroundColor;
	private Color currentBackgroundBrightColor = backgroundBrightColor;
	private Color currentTextColor = textColor;
	
	/**
	 * Following colors are set to default colors if not hovering over expand box
	 */
	private Color currentExpandBorderColor = arrowExpandBorderColor;
	private Color currentExpandBackgroundColor = arrowExpandBackgroundColor;
	
	/**
	 * parent action of this draw info
	 */
	private Action action;
	
	/**
	 * set to true if parameters need a recalculation.
	 * This is the case if properties/subactions/... have changed
	 */
	private boolean needsRecalculation = true;
	
	/**
	 * True if mouse if hovering over this action
	 */
	private boolean isHover = false;
	
	/**
	 * Width of the name text, used to calculate max box width
	 */
	private float nameWidth;
	
	/**
	 * Maximum width over all key texts, used to align properties text
	 */
	private float maxKeyWidth;
	/**
	 * Maximum width over all value texts, used to calculate max box width
	 */
	private float maxValueWidth;
	/**
	 * Height of properties all together. Used to calculate max box height
	 */
	private float propertiesHeight;
	
	/**
	 * Height of a single text line. Used for calculating text positions
	 */
	private float textHeight;
	
	/**
	 * Maximum box height over all parent actions of this action.
	 * Used to draw all parents with same height
	 */
	private float parentsMaxHeight;
	/**
	 * Maximum box height over all child actions of this action.
	 * Used to draw all parents with same height
	 */
	private float childrenMaxHeight;
	
	private float parentStartX;
	private float childStartX;
	private float boxOffsetLeft;
	
	/**
	 * Size of the sequence box containing the subsequences of an action.
	 */
	private Vector2f sequenceBoxDimension = new Vector2f();
	
	private float maxSubsequenceHeight = 0;
	
	private boolean hasExpandButton = false;
	
	/**
	 * current position to draw the main box of the action
	 */
	private Vector2f position;
	
	private Rectangle boundingBoxExtended = new Rectangle(0,0,0,0);
	private Rectangle boundingBoxSimple = new Rectangle(0,0,0,0);
	
	/**
	 * Constructor
	 * @param parent Parent action for which this draw infos are
	 */
	public ActionDrawInformation(Action parent)
	{
		action = parent;
	}
	
	private void notifyModifiedSubsequences(Action a)
	{
		if (a==null)
			return;
		a.getDrawInfo().notifyModified();
		notifyModifiedSubsequences(a.getExpandedSequence());
	}
	
	/**
	 * Call this if action properties/subsequences/parents/children changed.
	 * This tells the class to recalculate the dimensions of all boxes
	 */
	public void notifyModified()
	{
		needsRecalculation = true;
		notifyModifiedSubsequences(action.getExpandedSequence());
	}
	
	/**
	 * Height of title box of the action
	 * @return Height of the name box in pixels
	 */
	public float getNameBoxHeight()
	{
		return textHeight*3;
	}
	
	/**
	 * Get width of name in pixels.
	 * @return Width of name in pixels
	 */
	public float getNameWidth()
	{
		return nameWidth;
	}
	
	/**
	 * Get dimension of the simple box.
	 * The simple box contains only title and properties
	 * @return the dimension in pixels of the simple box
	 */
	public Vector2f getSimpleBoxDimension()
	{
		//Max with of name or properties
		float width = Math.max(nameWidth,maxKeyWidth+maxValueWidth)+ MAIN_BOX_PADDING*2;
		
		//Height of name box
		float height = getNameBoxHeight();
		
		height += propertiesHeight+INNER_CONTENT_PADDING + MAIN_BOX_PADDING;
		
		return new Vector2f(width, height);
	}
	
	/**
	 * Get dimension of the extended box.
	 * The extended box contains title, properties and subsequences
	 * @return the dimension in pixels of the simple box
	 */
	public Vector2f getExtendedBoxDimension()
	{
		Vector2f dim = getSimpleBoxDimension();
		dim.x = Math.max(dim.x,sequenceBoxDimension.x + 2*MAIN_BOX_PADDING);
		dim.y += sequenceBoxDimension.y + INNER_CONTENT_PADDING;
		
		return dim;
	}
	
	private static void checkAndAddSizeOfExpandedSequence(PApplet applet,Vector2f currentSequenceBoxSize, Action currAction)
	{
		if (currAction.getDrawInfo().needsRecalculation)
			currAction.getDrawInfo().recalculateDimensions(applet);
		if (currAction.getExpandedSequence() != null)
		{
			Action exp = currAction.getExpandedSequence();
			currentSequenceBoxSize.x = Math.max(currentSequenceBoxSize.x,exp.getDrawInfo().sequenceBoxDimension.x+2*SEQUENCE_BOX_PADDING);
			currentSequenceBoxSize.y += exp.getDrawInfo().sequenceBoxDimension.y;
		}
	}
	
	/**
	 * Recalculate the dimension of simple/extended box.
	 * Only executed if needsRecalculation is set to true (notifyModified called)
	 * @param applet Applet used to draw the action. Only used for text calculation.
	 * 	Because width of text is dependent on current text setting
	 */
	private void recalculateDimensions(PApplet applet)
	{
		if (!needsRecalculation)
			return;
		
		float totalWidth = 0;
		float totalHeight = 0;
		
		//Text dimension
		textHeight = applet.textAscent();
		nameWidth = applet.textWidth(action.getName());
		
		//Properties of the action
		maxKeyWidth = 0;
		maxValueWidth = 0;
		propertiesHeight = 0;
		Collection<String> keys = action.getProperties().keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext(); )
		{
			String key = it.next();
			String value = action.getProperty(key);
			maxKeyWidth = Math.max(maxKeyWidth, applet.textWidth(key+":  "));
			maxValueWidth = Math.max(maxValueWidth, applet.textWidth(value));
			propertiesHeight += textHeight* LINE_HEIGHT;
		}
		
		needsRecalculation = false;
		
		//Calculation for sequence boxes
		sequenceBoxDimension.x = SEQUENCE_BOX_PADDING;
		sequenceBoxDimension.y = 0;
		hasExpandButton = false;
		for (Iterator<Action> i = action.getSequenceIterator(); i.hasNext();)
		{
			Action a = i.next();
			if (a.getSequenceCount() > 0)
				hasExpandButton = true;
			ActionDrawInformation inf = a.getDrawInfo();
			
			inf.recalculateDimensions(applet);
			
			sequenceBoxDimension.x += inf.getSimpleBoxDimension().x + SEQUENCE_BOX_PADDING;
			sequenceBoxDimension.y = Math.max(sequenceBoxDimension.y, inf.getSimpleBoxDimension().y + 2*SEQUENCE_BOX_PADDING);
		}
		maxSubsequenceHeight = sequenceBoxDimension.y- 2*SEQUENCE_BOX_PADDING;
		if (hasExpandButton)
			sequenceBoxDimension.y += EXPAND_BOX_HEIGHT;
		checkAndAddSizeOfExpandedSequence(applet,sequenceBoxDimension,action);
		
		//Calculation for parent boxes
		parentsMaxHeight = 0;
		float parentsWidth = 0;
		for (Iterator<Action> i = action.getParentActionsIterator(); i.hasNext(); )
		{
			ActionDrawInformation inf = i.next().getDrawInfo();
			
			inf.recalculateDimensions(applet);
			
			parentsMaxHeight = Math.max(parentsMaxHeight, inf.getSimpleBoxDimension().y);
			parentsWidth += inf.getSimpleBoxDimension().x+MAIN_BOX_PADDING;
		}
		parentsWidth = Math.max(0, parentsWidth-MAIN_BOX_PADDING);
		
		totalWidth = Math.max(totalWidth, parentsWidth);
		totalHeight += parentsMaxHeight+MAIN_BOX_PADDING;
		
		//Calculation for child boxes
		childrenMaxHeight = 0;
		float childrenWidth = 0;
		for (Iterator<Action> i = action.getChildActionsIterator(); i.hasNext(); )
		{
			ActionDrawInformation inf = i.next().getDrawInfo();
			
			inf.recalculateDimensions(applet);
			
			childrenMaxHeight = Math.max(childrenMaxHeight, inf.getSimpleBoxDimension().y);
			childrenWidth += inf.getSimpleBoxDimension().x+MAIN_BOX_PADDING;
		}
		childrenWidth = Math.max(0, childrenWidth-MAIN_BOX_PADDING);
		totalWidth = Math.max(totalWidth, childrenWidth);
		totalHeight += childrenMaxHeight+MAIN_BOX_PADDING;
		
		this.boxOffsetLeft = Math.max(0, Math.max((parentsWidth-getExtendedBoxDimension().x)/2f,(childrenWidth-getExtendedBoxDimension().x)/2f));
		
		this.parentStartX = Math.max(0, getExtendedBoxDimension().x/2f-parentsWidth/2f);
		this.childStartX = Math.max(0, getExtendedBoxDimension().x/2f-childrenWidth/2f);
		
		totalWidth = Math.max(totalWidth, getExtendedBoxDimension().x);
		totalHeight += getExtendedBoxDimension().y;
		
		boundingBoxExtended.width = (int)totalWidth;
		boundingBoxExtended.height = (int)totalHeight;
		
		boundingBoxSimple.width = (int)getSimpleBoxDimension().x;
		boundingBoxSimple.height = (int)getSimpleBoxDimension().y;

	}
	
	/**
	 * Draw border and title of the action with given dimension at given position
	 * @param applet Applet to draw on
	 * @param position position where to draw the box
	 * @param dimension dimension of the box to draw
	 */
	private void drawBorderAndTitle(PApplet applet, Vector2f position, Vector2f dimension)
	{
		//Draw outer border and background
	    applet.fill(currentBackgroundColor.getRed(), currentBackgroundColor.getGreen(), currentBackgroundColor.getBlue(), currentBackgroundColor.getAlpha());
		applet.rect(position.x, position.y, dimension.x, dimension.y);
		
		//Draw box title
		applet.fill(currentBackgroundBrightColor.getRed(), currentBackgroundBrightColor.getGreen(), currentBackgroundBrightColor.getBlue(), currentBackgroundBrightColor.getAlpha());
		applet.rect(position.x,position.y,dimension.x, getNameBoxHeight());
		applet.fill(currentTextColor.getRed(), currentTextColor.getGreen(), currentTextColor.getBlue(), currentTextColor.getAlpha());
		applet.text(action.getName(),position.x + MAIN_BOX_PADDING,position.y + textHeight*2f);
	}
	
	/**
	 * Draw the action properties beginning at the given position.
	 * @param applet Applet to draw on
	 * @param position The upper left position to begin the drawing
	 */
	private void drawProperties(PApplet applet, Vector2f position)
	{
		applet.fill(currentTextColor.getRed(), currentTextColor.getGreen(), currentTextColor.getBlue(), currentTextColor.getAlpha());
		Collection<String> keys = action.getProperties().keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext(); )
		{
			String key = it.next();
			String value = action.getProperty(key);
			
			applet.text(key + ":",position.x,position.y+textHeight);
			applet.text(value,position.x + maxKeyWidth, position.y+textHeight);

			position.y += textHeight * LINE_HEIGHT;
		}
	}
	
	/**
	 * Draw the simple box. Contains title and properties
	 * @param applet Applet to draw on
	 * @param position Position where to draw the box
	 * @param minHeight minimum height of the box.
	 */
	public void drawSimpleBox(PApplet applet,Vector2f position, float minHeight)
	{
		drawSimpleBox(applet, position, minHeight,false);
	}
	
	/**
	 * Draw the simple box. Contains title and properties
	 * @param applet Applet to draw on
	 * @param position Position where to draw the box
	 * @param minHeight minimum height of the box.
	 */
	public void drawSimpleBox(PApplet applet,Vector2f position, float minHeight, boolean drawExpandBox)
	{
		this.drawnAsSimple = true;
		this.position = new Vector2f(position);
		boundingBoxSimple.x = (int)position.x;
		boundingBoxSimple.y = (int)position.y;
		Vector2f tmpPos = new Vector2f(position);
		recalculateDimensions(applet);
		
		applet.stroke(currentBorderColor.getRed(), currentBorderColor.getGreen(), currentBorderColor.getBlue(), currentBorderColor.getAlpha());
		drawBorderAndTitle(applet, tmpPos,new Vector2f(getSimpleBoxDimension().x,Math.max(minHeight, getSimpleBoxDimension().y)));		
		
		if (drawExpandBox)
		{
			applet.stroke(currentExpandBorderColor.getRed(), currentExpandBorderColor.getGreen(), currentExpandBorderColor.getBlue(), currentExpandBorderColor.getAlpha());
		    applet.fill(currentExpandBackgroundColor.getRed(), currentExpandBackgroundColor.getGreen(), currentExpandBackgroundColor.getBlue(), currentExpandBackgroundColor.getAlpha());
		    

			Vector2f from = new Vector2f(tmpPos.x+getSimpleBoxDimension().x/2f,tmpPos.y+Math.max(minHeight, getSimpleBoxDimension().y)+4);
			Vector2f to = new Vector2f(tmpPos.x+getSimpleBoxDimension().x/2f,tmpPos.y+Math.max(minHeight, getSimpleBoxDimension().y)+EXPAND_BOX_HEIGHT-4);
		    if (action.isExpanded())
		    	PlanVisApplet.arrowFromTo(applet, to, from, 6,0);
		    else
		    	PlanVisApplet.arrowFromTo(applet, from, to, 6,0);
		}
		
		tmpPos.x += MAIN_BOX_PADDING;
		tmpPos.y += getNameBoxHeight()+INNER_CONTENT_PADDING;
		
		drawProperties(applet, tmpPos);
	}
	
	/**
	 * Draw the parent boxes of this action. These are located over the action box
	 * @param applet Applet to draw on
	 * @param position start position where to begin to draw
	 * @return ArrayList of Vector2f with the points at the center bottom for drawing connection arrows
	 */
	private ArrayList<Vector2f> drawParentBoxes(PApplet applet, Vector2f position)
	{
		Vector2f tmpPos = new Vector2f(position);
		ArrayList<Vector2f> retPoints = new ArrayList<Vector2f>();
		
		for (Iterator<Action> i = action.getParentActionsIterator(); i.hasNext(); )
		{
			ActionDrawInformation inf = i.next().getDrawInfo();
			
			inf.drawSimpleBox(applet, tmpPos, parentsMaxHeight);
		
		    retPoints.add(new Vector2f(tmpPos.x + inf.getSimpleBoxDimension().x/2f, tmpPos.y+parentsMaxHeight-5));
	
			tmpPos.x += inf.getSimpleBoxDimension().x + MAIN_BOX_PADDING;
		}
		return retPoints;
	}
	
	/**
	 * Draw the cild boxes of this action. These are located under the extended action box
	 * @param applet Applet to draw on
	 * @param position start position where to begin to draw
	 * @return ArrayList of Vector2f with the points at the center bottom for drawing connection arrows
	 */
	private ArrayList<Vector2f> drawChildrenBoxes(PApplet applet, Vector2f position)
	{
		Vector2f tmpPos = new Vector2f(position);
		ArrayList<Vector2f> retPoints = new ArrayList<Vector2f>();
		
		for (Iterator<Action> i = action.getChildActionsIterator(); i.hasNext(); )
		{
			ActionDrawInformation inf = i.next().getDrawInfo();
			
			inf.drawSimpleBox(applet, tmpPos, childrenMaxHeight);
			
		    retPoints.add(new Vector2f(tmpPos.x + inf.getSimpleBoxDimension().x/2f, tmpPos.y+5));
			
			tmpPos.x += inf.getSimpleBoxDimension().x + MAIN_BOX_PADDING;
		}
		return retPoints;
	}
	
	/**
	 * Connect each point of parentPoints with the current action box. Do the same with child points.
	 * The target points will be calculated by this function with a homogeneous distribution over the
	 * whole with of the current action box.
	 * 
	 * @param applet Applet to draw on
	 * @param parentPoints Connection points of parent actions
	 * @param childPoints Connection points of child actions
	 */
	private void drawArrows(PApplet applet, ArrayList<Vector2f> parentPoints, ArrayList<Vector2f> childPoints)
	{
		applet.stroke(arrowBorderColor.getRed(), arrowBorderColor.getGreen(), arrowBorderColor.getBlue(), arrowBorderColor.getAlpha());
	    applet.fill(arrowBackgroundColor.getRed(), arrowBackgroundColor.getGreen(), arrowBackgroundColor.getBlue(), arrowBackgroundColor.getAlpha());

	    Vector2f connPointParent = new Vector2f(position);
	    connPointParent.x+=boxOffsetLeft;
	    Vector2f connPointChildren = new Vector2f(connPointParent);
	    connPointChildren.y += getExtendedBoxDimension().y-5;
	    connPointParent.y += 5;
	    
	    float diffParent = getExtendedBoxDimension().x / (parentPoints.size()+1);
	    
	    for (int i=0; i<parentPoints.size(); i++)
	    {
	    	connPointParent.x += diffParent;
	    	PlanVisApplet.arrowFromTo(applet, parentPoints.get(i), connPointParent, 5,-1);
	    }
	    
	    float diffChild = getExtendedBoxDimension().x / (childPoints.size()+1);
	    
	    for (int i=0; i<childPoints.size(); i++)
	    {
	    	connPointChildren.x += diffChild;
	    	PlanVisApplet.arrowFromTo(applet, connPointChildren,childPoints.get(i), 5,-1);
	    }	    
	}

	/**
	 * Draw the subsequence boxes of current action.
	 * @param applet Applet to draw on
	 * @param position Start position (upper left corner)
	 */
	private void drawSequence(PApplet applet, Vector2f position, boolean isExpandedBox)
	{
		//Draw outer box of sequence list
		if (!action.getSequenceIterator().hasNext())
			return;
		
		applet.stroke(currentBorderColor.getRed(), currentBorderColor.getGreen(), currentBorderColor.getBlue(), currentBorderColor.getAlpha());
	    if (isExpandedBox)
	    	applet.fill(currentBackgroundColor.getRed(), currentBackgroundColor.getGreen(), currentBackgroundColor.getBlue(), currentBackgroundColor.getAlpha());
	    else
	    	applet.fill(currentBackgroundBrightColor.getRed(), currentBackgroundBrightColor.getGreen(), currentBackgroundBrightColor.getBlue(), currentBackgroundBrightColor.getAlpha());
		applet.rect(position.x, position.y, sequenceBoxDimension.x, sequenceBoxDimension.y);
		
		//Calculate start pos of inner boxes
		Vector2f tmpPos = new Vector2f(position);
		tmpPos.y += SEQUENCE_BOX_PADDING;
		tmpPos.x += SEQUENCE_BOX_PADDING;
		
		//Store the positions of each box to connect them afterwards
		ArrayList<Float> pos = new ArrayList<Float>();

		Vector2f expandedParentCorner1 = null;
		Vector2f expandedParentCorner2 = null;

		Action expSeq = action.getExpandedSequence();
		
		
		//Draw inner boxes
		for (Iterator<Action> i = action.getSequenceIterator(); i.hasNext();)
		{
			Action a = i.next();
			ActionDrawInformation inf = a.getDrawInfo();
			
			inf.drawSimpleBox(applet, tmpPos, maxSubsequenceHeight,a.getSequenceCount()>0);
			if (a == expSeq)
			{
				expandedParentCorner1 = new Vector2f(tmpPos.x,tmpPos.y+maxSubsequenceHeight);
				expandedParentCorner2 = new Vector2f(tmpPos.x+inf.getSimpleBoxDimension().x,tmpPos.y+maxSubsequenceHeight);
			}
			
			tmpPos.x += inf.getSimpleBoxDimension().x + SEQUENCE_BOX_PADDING;
			pos.add(tmpPos.x);
		}
		
		//Draw connection arrows for each sequence box
		for (int i=0; i<pos.size()-1; i++)
		{
			applet.stroke(arrowBorderColor.getRed(), arrowBorderColor.getGreen(), arrowBorderColor.getBlue(), arrowBorderColor.getAlpha());
		    applet.fill(arrowBackgroundColor.getRed(), arrowBackgroundColor.getGreen(), arrowBackgroundColor.getBlue(), arrowBackgroundColor.getAlpha());
			PlanVisApplet.arrow(applet,pos.get(i)-SEQUENCE_BOX_PADDING - 5,tmpPos.y+getNameBoxHeight()/2-10,20,SEQUENCE_BOX_PADDING+11);
		}
		
		//Draw expanded subsequences if any
		Vector2f startPos = new Vector2f(position);
		startPos.y +=SEQUENCE_BOX_PADDING + maxSubsequenceHeight + EXPAND_BOX_HEIGHT;
		
		if (expSeq != null && expandedParentCorner1 != null && expandedParentCorner2 != null)
		{
			Vector2f subDim = expSeq.getDrawInfo().sequenceBoxDimension;
			startPos.x = (expandedParentCorner2.x+expandedParentCorner1.x)/2f;
			startPos.x -= subDim.x/2f;
			
			startPos.x = Math.max(startPos.x, position.x+SEQUENCE_BOX_PADDING);
			
			startPos.x -= Math.max(0, (startPos.x +subDim.x) - (position.x+sequenceBoxDimension.x-SEQUENCE_BOX_PADDING));
			
			
			//applet.stroke(currentBorderColor.getRed(), currentBorderColor.getGreen(), currentBorderColor.getBlue(), currentBorderColor.getAlpha());
	    	//applet.fill(currentBackgroundColor.getRed(), currentBackgroundColor.getGreen(), currentBackgroundColor.getBlue(), currentBackgroundColor.getAlpha());
			//applet.line(expandedParentCorner1.x, expandedParentCorner1.y, startPos.x, startPos.y);
			//applet.line(expandedParentCorner2.x, expandedParentCorner2.y, startPos.x+subDim.x, startPos.y);
			
			expSeq.getDrawInfo().drawSequence(applet, startPos, true);
		}
	}
	
	/**
	 * Draw the extended action box which contains properties and subsequences. Also parent and child actions will be drawn.
	 * @param applet Applet to draw on.
	 * @param position Upper left corner where to begin to draw. If the action has parent action(s) it will be the position of
	 * the first parent box. If none parent actions are present, this will be the upper left corner of the extended box.
	 */
	public void drawExtendedBox(PApplet applet, Vector2f position)
	{
		this.drawnAsSimple = false;
		recalculateDimensions(applet);
		
		boundingBoxExtended.x = (int)position.x;
		boundingBoxExtended.y = (int)position.y;
		
		Vector2f extendedDim = getExtendedBoxDimension();
		
		//Draw parents
		ArrayList<Vector2f> parentPoints = drawParentBoxes(applet, new Vector2f(position.x + parentStartX,position.y));
		
		position.y += parentsMaxHeight+MAIN_BOX_PADDING;

		//Border & title
		this.position = new Vector2f(position);
		applet.strokeWeight(currentStroke);
		applet.stroke(hoverBorderColor.getRed(), hoverBorderColor.getGreen(), hoverBorderColor.getBlue(), hoverBorderColor.getAlpha());
		position.x += boxOffsetLeft;
		drawBorderAndTitle(applet, position,extendedDim);	
		applet.strokeWeight(defaultStroke);	
		
		position.x += MAIN_BOX_PADDING;
		position.y += getNameBoxHeight()+INNER_CONTENT_PADDING;
		
		//Properties
		drawProperties(applet, position);

		position.y += INNER_CONTENT_PADDING;
		
		//Subsequences
		drawSequence(applet, position, false);
		
		position.y += sequenceBoxDimension.y;
		position.x -= MAIN_BOX_PADDING;
		position.x -= boxOffsetLeft;
		
		position.y += MAIN_BOX_PADDING*2;
		//Children
		ArrayList<Vector2f> childPoints = drawChildrenBoxes(applet, new Vector2f(position.x + childStartX,position.y));
		
		drawArrows(applet, parentPoints, childPoints);
	}
	
	/**
	 * Set the correct colors when hovering over action.
	 * If mouse if over action, the hover* colors will be used to draw it.
	 * @param hover If true use hover* colors, otherwise use normal colors
	 */
	private void setHover(boolean hover)
	{
		if (hover)
		{
			currentBorderColor = hoverBorderColor;
			currentBackgroundColor = hoverBackgroundColor;
			currentBackgroundBrightColor = hoverBackgroundBrightColor;
			currentTextColor = hoverTextColor;
		} else 
		{
			currentBorderColor = borderColor;
			currentBackgroundColor = backgroundColor;
			currentBackgroundBrightColor = backgroundBrightColor;
			currentTextColor = textColor;
		}
		isHover = hover;
	}
	
	/**
	 * Set the correct colors when hovering over expand arrow.
	 * If mouse if over expand arrow, the hover* colors will be used to draw it.
	 * @param hover If true use hover* colors, otherwise use normal colors
	 */
	private void setHoverExpand(boolean hover)
	{
		if (hover || action.isExpanded())
		{
			currentExpandBorderColor = hoverArrowExpandBorderColor;
			currentExpandBackgroundColor = hoverArrowExpandBackgroundColor;
		} else 
		{
			currentExpandBorderColor = arrowExpandBorderColor;
			currentExpandBackgroundColor = arrowExpandBackgroundColor;
		}
	}
	
	private ActionDrawInformation checkHoverExtendedSequence(float x, float y, Action currAction, ActionDrawInformation found)
	{
		if (currAction ==null)
			return null;
		//over sequences?
		for (Iterator<Action> i = currAction.getSequenceIterator(); i.hasNext();)
		{
			ActionDrawInformation inf = i.next().getDrawInfo();
			
			if (found != null)
				inf.setHover(false);
			else {
				found = inf.checkHover(x, y,found);	
			}
		}
		return found;
	}
	
	/**
	 * Check if mouse position x|y is over this action
	 * @param x x coordinate of mouse
	 * @param y y coordinate of mouse
	 * @param foundInParent set to true if aleady a matching box was found, so only setHover(false) is needed to call 
	 * @return the Action (it's draw info) over which the mouse is hovering or null if none.
	 */
	private ActionDrawInformation checkHover(float x, float y, ActionDrawInformation found)
	{

		if (position == null)
			return found;
			
		//over extended sequences?
		found = checkHoverExtendedSequence(x,y,action.getExpandedSequence(), found);
		
		//Check for extended box
		if (!drawnAsSimple)
		{

			//over sequences?
			for (Iterator<Action> i = action.getSequenceIterator(); i.hasNext();)
			{
				ActionDrawInformation inf = i.next().getDrawInfo();
				
				if (found != null)
					inf.setHover(false);
				else
					if (inf.checkHover(x, y,found)!=null)
						found = inf;
			}
			
			//over parent?
			for (Iterator<Action> i = action.getParentActionsIterator(); i.hasNext(); )
			{
				ActionDrawInformation inf = i.next().getDrawInfo();
				
				if (found != null)
					inf.setHover(false);
				else
					if (inf.checkHover(x, y,found)!=null)
						found = inf;
			}
			
			//over child
			for (Iterator<Action> i = action.getChildActionsIterator(); i.hasNext(); )
			{
				ActionDrawInformation inf = i.next().getDrawInfo();
				
				if (found != null)
					inf.setHover(false);
				else
					if (inf.checkHover(x, y,found)!=null)
						found = inf;
			}
			
			//not hovering, so set correct colors
			setHover(false);
			return found; //will be null
			
			
			
		} else {
			//Check hovering if drawn as simple box
			Vector2f dim = getSimpleBoxDimension();
			float boxX,boxY,boxW,boxH;
			boxX = position.x;
			boxY = position.y;
			boxW = dim.x;
			boxH = dim.y;
			if (x>boxX && x<boxX+boxW && y>boxY && y<boxY+boxH)
			{
				setHover(true);
				return this;
			} else {
				setHover(false);
				return found;
			}
		}
		
	}
	
	/**
	 * Check if mouse position x|y is over an expand arrow
	 * @param x x coordinate of mouse
	 * @param y y coordinate of mouse
	 * @param sequenceExpandHeight set to 0 for direct call. Only used for recursing
	 * @return the parent Action (it's draw info) over which's expand arrow the mouse is hovering or null if none.
	 */
	private ActionDrawInformation checkHoverExpand(float x, float y, boolean hasExpandButton, float sequenceBoxHeight, boolean parentExpanded)
	{

		if (position == null)
			return null;
		ActionDrawInformation found = null;
			
		//over sequences?
		if (parentExpanded)
		{
			for (Iterator<Action> i = action.getSequenceIterator(); i.hasNext();)
			{
				Action a = i.next();
				ActionDrawInformation inf = a.getDrawInfo();
				
				if (found != null)
					inf.setHoverExpand(false);
				else
				{
					ActionDrawInformation d = inf.checkHoverExpand(x, y,this.hasExpandButton,this.maxSubsequenceHeight, a.isExpanded());
					if (d!=null)
						found = d;
				}
			}
		}
		
		if (hasExpandButton) {
			//Check hovering if drawn as simple box
			Vector2f dim = getSimpleBoxDimension();
			float boxX,boxY,boxW,boxH;
			boxX = position.x;
			boxY = position.y+sequenceBoxHeight;
			boxW = dim.x;
			boxH = EXPAND_BOX_HEIGHT;
			if (action.getSequenceCount() > 0 && x>boxX && x<boxX+boxW && y>boxY && y<boxY+boxH)
			{
				setHoverExpand(true);
				return this;
			} else {
				setHoverExpand(false);
				return found;
			}
		}
		
		return found;
		
	}
	
	/**
	 * Update color of action for current mouse position.
	 * @param x x coordinate of mouse
	 * @param y y coordinate of mouse
	 * @return true if hovering, false if not
	 */
	public boolean updateHover(float x, float y)
	{
		boolean found = checkHover(x,y,null)!=null;
		return (checkHoverExpand(x, y, false,0,true)!=null || found);
	}
	
	/**
	 * Check if mouse click on position x|y was intended for an action.
	 * @param x x coordinate of mouse
	 * @param y y coordinate of mouse
	 * @return Action which is under given coordinate of null if none
	 */
	public Action checkClick(float x, float y)
	{
		ActionDrawInformation a = checkHover(x,y,null);
		if (a==null)
			return null;
		else
			return a.action;
	}
	
	/**
	 * Check if mouse click on position x|y was intended for an expand button.
	 * @param x x coordinate of mouse
	 * @param y y coordinate of mouse
	 * @return Action for which this expand button is
	 */
	public Action checkClickExpand(float x, float y)
	{
		ActionDrawInformation a = checkHoverExpand(x, y, false,0,true);
		if (a==null)
			return null;
		else
			return a.action;
	}
	
	/**
	 * Get height of drawing text
	 * @return
	 */
	public float getTextHeight()
	{
		return textHeight;
	}
	
	/**
	 * Get action state if mouse is hovering over this action.
	 * @return true if hovering
	 */
	public boolean IsHover()
	{
		return isHover;
	}
	
	public Rectangle getBoundingBox()
	{
		if (drawnAsSimple)
			return boundingBoxSimple;
		else
			return boundingBoxExtended;
	}
}
