/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.core.utilities.md.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Block;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.vars.VariableInteractor;
import ru.dip.core.unit.md.MarkdownSettings;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.md.BulletMdList;
import ru.dip.core.utilities.md.ListItemTextWrapper;
import ru.dip.core.utilities.md.MdList;
import ru.dip.core.utilities.md.MdNode;
import ru.dip.core.utilities.md.OrdereMdList;
import ru.dip.core.utilities.md.parser.latex.LatexModel;
import ru.dip.core.utilities.md.parser.latex.LatexText;

public class MdPresentationParser {
	
	// format points
	private List<Point> fBoldPoints; 
	private List<Point> fItalicPoints;
	private List<Point> fBoldItalicPoints;
	private List<Point> fCodePoints;
	private List<Point> fFencedPoints;
	private List<Point> fCommentPoints;
	// settings
	private boolean fFontStylesEnable;
	private boolean fShowEmptyLines = true;
	private boolean fListEmptyLines = true;
	private boolean fIndentEnable = true;
	private boolean fShowComment = true;
	private String fIndent = "          ";
	
	private StringBuilder fBuilder = new StringBuilder();
	private Stack<MdList> fListStack; 
	private HashMap<Node, MdNode> fNodes = new HashMap<>();
	private Visitor fVisitor = new Visitor();
	private int fLineLength;  // длина строки (для переноса текста)
	private IDipUnit fUnit;
	private LatexModel fLatexModel;
	
	public MdPresentationParser(IDipUnit unit) {
		fUnit = unit;
	}
	
	//================================
	// parse file
	
	public String parse(MarkdownSettings settings, int lineLength, IFile file){
		initSetting(settings);	
		fLineLength = lineLength; 
		try {
			return parse(file);
		} catch (IOException e) {	
			DipCorePlugin.logError(e, "Read markdown error");
			e.printStackTrace();
			return "Read markdown error";
		}
	}
	
	private String parse(IFile file) throws IOException{
		fNodes.clear();
		String input = FileUtilities.readFile(file);
		input = VariableInteractor.changeVar(input, fUnit);
		return parse(input);
	}
	
	//=========================
	// parse text
	
	public  String parse(MarkdownSettings settings, int lineLength, String text){
		if (text == null) {
			text = "";
		}
		initSetting(settings);	
		fLineLength = lineLength; 
		return parse(text);
	}
	
	private String parse(String text){
		fLatexModel = new LatexModel();
		text = fLatexModel.parseOriginalMardown(text);
		fNodes.clear();
		MdParser parser = MdParser.instance();
		Node document = parser.parse(text, fUnit);
		fVisitor.setFirst();
		document.accept(fVisitor);
		return fBuilder.toString();
	}
	

	class Visitor extends AbstractVisitor {
	
		boolean first = true; 
		
		public void setFirst(){
			first = true;
		}
		//====  visit(Image, BlockQuote, Link, StrongEmphasis, Emphasis, ThematicBreak)
		// don nothing
		
		private MdNode createMdNode(Node node) {
			MdNode mdNode = new MdNode(node);
			mdNode.setStart(fBuilder.length());
			fNodes.put(node, mdNode);		
			Node parentNode = node.getParent();
			if (parentNode != null) {
				MdNode parentMdNode = fNodes.get(parentNode);
				if (parentMdNode != null) {
					parentMdNode.addChild(mdNode);
				}
			}
			return mdNode;
		}
				
		@Override
		protected void visitChildren(Node node) {
			super.visitChildren(node);		
		}

		@Override
		public void visit(Text text) {
			List<IMdText> texts = fLatexModel.parseContentWithLatexKeys(text.getLiteral());
			for (IMdText mdText: texts) {
				if (mdText instanceof MdText) {
					visitText(mdText.getText(), text);
				} else if (mdText instanceof LatexText) {
					visitCode(mdText.getText(), text);
				}
			}
			

		}
		
		private void visitText(String literal, Text text) {
			MdNode mdNode = createMdNode(text);
			Node parent = text.getParent();
			//String literal = text.getLiteral();
			if (fFontStylesEnable){
				if (parent instanceof StrongEmphasis){	
					if (parent.getParent() instanceof Emphasis){
						int x = fBuilder.length();
						int y = text.getLiteral().length();
						fBoldItalicPoints.add(new Point(x,y));
					} else {
						int x = fBuilder.length();
						int y = text.getLiteral().length();
						fBoldPoints.add(new Point(x,y));
					}						
				} else if (parent instanceof Emphasis){
					int x = fBuilder.length();
					int y = text.getLiteral().length();
					fItalicPoints.add(new Point(x,y));
				}
			}	
			if (parent instanceof Link){
				Link link = (Link) parent;
				String destination = link.getDestination();
				String ref = LinkInteractor.instance().getRef(literal, destination, fUnit);
				fBuilder.append(ref);								
			} else {
				fBuilder.append(literal);	
			}				
			super.visit(text);
			mdNode.setEnd(fBuilder.length());
		}
		
		
		@Override
		public void visit(Link link) {
			MdNode mdNode = createMdNode(link);
			if (link.getFirstChild() == null) {
				String destination = link.getDestination();
				String ref = LinkInteractor.instance().getEmptyRef(fUnit, destination);										
				fBuilder.append(ref);
			}
			super.visit(link);
			mdNode.setEnd(fBuilder.length());
		}
		
		@Override
		public void visit(Heading heading) {
			MdNode mdNode = createMdNode(heading);
			addEmptyLine();
			addIndent();
			for (int i = 0; i < heading.getLevel(); i++){
				fBuilder.append("#");
			}				
			super.visit(heading);
			mdNode.setEnd(fBuilder.length());

		}			
		
		@Override
		public void visit(HardLineBreak hardLineBreak) {
			MdNode mdNode = createMdNode(hardLineBreak);
			fBuilder.append("\n");
			super.visit(hardLineBreak);
			mdNode.setEnd(fBuilder.length());
		}
		
		@Override
		public void visit(SoftLineBreak softLineBreak) {
			MdNode mdNode = createMdNode(softLineBreak);
			fBuilder.append(" ");
			super.visit(softLineBreak);
			mdNode.setEnd(fBuilder.length());
		}

		 @Override
		public void visit(IndentedCodeBlock indentedCodeBlock) {
			MdNode mdNode = createMdNode(indentedCodeBlock);
			if (indentedCodeBlock.getPrevious() instanceof Paragraph){
				addEmptyLine();
			}
			// indent
			addIndent();
			fBuilder.append(indentedCodeBlock.getLiteral());
			super.visit(indentedCodeBlock);
			mdNode.setEnd(fBuilder.length());
		}
		
		@Override
		public void visit(Paragraph paragraph) {
			MdNode mdNode = createMdNode(paragraph);
			Block parent = paragraph.getParent();
			boolean isList = parent instanceof ListItem;			
			if (isList) {
				visitListParagraph((ListItem) parent);
			} else {
				visitParagraph();
			}
			super.visit(paragraph);
			mdNode.setEnd(fBuilder.length());
		}
					
		private void visitParagraph() {
			// empty line
			addEmptyLine();
			// indent
			addIndent();
		}
		
		private void visitListParagraph(ListItem item) {
			// empty line
			if (first){
				first = false;
			} else  {
				fBuilder.append("\n");			
				if (fShowEmptyLines && fListEmptyLines){
						fBuilder.append("\n");							
				} 				
			}
			// indent
			addIndent();				
			// add list item indent
			for (int i = 1; i < fListStack.size(); i++){
				fBuilder.append(fIndent);
			}					
			fBuilder.append(fListStack.lastElement().currentMarker());
		}
		
		@Override
		public void visit(BulletList bulletList) {
			MdNode mdNode = createMdNode(bulletList);
			MdList mdList = new BulletMdList(bulletList);
			getParent(bulletList);
			fListStack.push(mdList);
			super.visit(bulletList);
			mdNode.setEnd(fBuilder.length());
		}
		
		@Override
		public void visit(OrderedList orderedList) {
			MdNode mdNode = createMdNode(orderedList);
			MdList mdList = new OrdereMdList(orderedList);
			getParent(orderedList);
			fListStack.push(mdList);
			super.visit(orderedList);
			mdNode.setEnd(fBuilder.length());
		}
		
		@Override
		public void visit(ListItem listItem) {
			MdNode mdNode = createMdNode(listItem);
			super.visit(listItem);
			getParent(listItem);
			mdNode.setEnd(fBuilder.length());			
			// wrap text
			String marker = fListStack.lastElement().evaluateListMarker();		
			int start = mdNode.start();	
			int end = mdNode.end();
			String content = fBuilder.subSequence(start, end).toString();
			ListItemTextWrapper textWrapper = new ListItemTextWrapper(content, fLineLength,  marker.length());		
			String newContent = textWrapper.newContent();	
			// update style points
			textWrapper.updatePoints(fBoldPoints.stream().filter(p -> p.x > start && p.x + p.y < end), start);
			textWrapper.updatePoints(fItalicPoints.stream().filter(p -> p.x > start && p.x + p.y < end), start);
			textWrapper.updatePoints(fBoldItalicPoints.stream().filter(p -> p.x > start && p.x + p.y < end), start);
			textWrapper.updatePoints(fCodePoints.stream().filter(p -> p.x > start && p.x + p.y < end), start);
			textWrapper.updatePoints(fFencedPoints.stream().filter(p -> p.x > start && p.x + p.y < end), start);
			textWrapper.updatePoints(fCommentPoints.stream().filter(p -> p.x > start && p.x + p.y < end), start);
			fBuilder.replace(start, end, newContent);
			mdNode.setEnd(fBuilder.length());
		}
				
		@Override
		public void visit(Code code) {
			visitCode(code.getLiteral(), code);
		}
		
		private void visitCode(String literal, Node code) {
			MdNode mdNode = createMdNode(code);
			int x = fBuilder.length();
			int y = literal.length() - 1;
			fCodePoints.add(new Point(x,y));
			fBuilder.append(literal);
			//super.visit(code);
			mdNode.setEnd(fBuilder.length());
		}
		
		
		@Override
		public void visit(FencedCodeBlock code) {
			MdNode mdNode = createMdNode(code);
			addEmptyLine();
			int x = fBuilder.length();
			String codeText = code.getLiteral();
			if (codeText.isEmpty()) {
				codeText = code.getInfo();
			}
			if  (codeText.endsWith(TagStringUtilities.lineSeparator())) {
				codeText = codeText.substring(0, codeText.length() - 1);
			}
			int y = codeText.length();
			fFencedPoints.add(new Point(x,y));
			fBuilder.append(codeText);	
			super.visit(code);
			mdNode.setEnd(fBuilder.length());
		}
					
		@Override
		public void visit(HtmlBlock htmlBlock) {
			MdNode mdNode = createMdNode(htmlBlock);
			if (!fShowComment) {
				return;
			}				
			String literal = htmlBlock.getLiteral().trim();
			if (literal.startsWith("<!--") && literal.endsWith("-->")) {
				addEmptyLine();		
				addIndent();
				literal = literal.substring(4, literal.length() - 3).trim();
				int x = fBuilder.length(); 										
				fBuilder.append(literal);
				int y = literal.length() - 1;
				fCommentPoints.add(new Point(x,y));
			}
			super.visit(htmlBlock);
			mdNode.setEnd(fBuilder.length());
		}
		
		@Override
		public void visit(HtmlInline htmlInline) {
			MdNode mdNode = createMdNode(htmlInline);
			String literal = htmlInline.getLiteral();
			if (literal.startsWith("<!--") && literal.endsWith("-->")) {
				if (!fShowComment) {
					return;
				}
				literal = literal.substring(4, literal.length() - 3).trim();
				int x = fBuilder.length(); 										
				fBuilder.append(literal);
				int y = literal.length() - 1;
				fCommentPoints.add(new Point(x,y));
			}
			super.visit(htmlInline);
			mdNode.setEnd(fBuilder.length());
		}
		
		private MdList getParent(Node list){
			Node parent = list.getParent();
			if (parent instanceof ListItem){
				ListItem item = (ListItem) parent;
				return getParent(item);
			}
						
			while (!fListStack.isEmpty()){
				MdList last = fListStack.lastElement();
				if (last.isParent(list)){
					return last;
				}
				fListStack.pop();
			}
			return null;
		}
		
		private MdList getParent(ListItem item){
			while (!fListStack.isEmpty()){
				MdList last = fListStack.lastElement();
				if (last.isParent(item)){
					return last;
				}
				fListStack.pop();
			}
			return null;
		}	
		
		private void addEmptyLine() {
			if (first){
				first = false;
			} else {
				fBuilder.append("\n");
				if (fShowEmptyLines) {
					fBuilder.append("\n");
				}
			}
		}
	
		private void addIndent() {
			if (fIndentEnable){
				fBuilder.append(fIndent);
			}	
		}
	};
	
	//==============================
	// settings
	
	private void initSetting(MarkdownSettings settings)	{
		cleanLists();
		setSettings(settings);
	}
	
	private void cleanLists() {
		fListStack = new Stack<>();
		fBuilder = new StringBuilder();
		fBoldPoints = new ArrayList<>();
		fItalicPoints = new ArrayList<>();
		fBoldItalicPoints = new ArrayList<>();
		fCodePoints = new ArrayList<>();
		fFencedPoints = new ArrayList<>();
		fCommentPoints = new ArrayList<>();
	}
	
	private void setSettings(MarkdownSettings settings) {
		fFontStylesEnable = settings.fontStylesEnable();
		fShowEmptyLines = settings.emptyLines();
		fIndentEnable = settings.indentEnable();
		fListEmptyLines = settings.listEmptyLines();
		fShowComment = settings.showComment();
		
		StringBuilder indentBuilder = new StringBuilder();
		for (int i = 0; i < settings.indent(); i++){
			indentBuilder.append(" ");
		}
		fIndent = indentBuilder.toString();
	}
	
	
	//===========================
	// get result format points
	
	/**
	 * Добавляет смещение к точкам (boldPoints, italicPoints и  т.д.)
	 */
	public void addOffset(int offset) {
		fCommentPoints.forEach(p -> p.x += offset);
		fFencedPoints.forEach(p -> p.x += offset);
		fBoldPoints.forEach(p -> p.x += offset);
		fItalicPoints.forEach(p -> p.x += offset);
		fBoldItalicPoints.forEach(p -> p.x += offset);
		fCodePoints.forEach(p -> p.x += offset);
	}
	
	public List<Point> codePoints(){
		return fCodePoints;
	}
	
	public List<Point> fencedCodePoints(){
		return fFencedPoints;
	}
	
	public List<Point> commentPoints(){
		return fCommentPoints;
	}
	
	public List<Point> boldPoints(){
		return fBoldPoints;
	}
	
	public List<Point> italicPoints(){
		return fItalicPoints;
	}
	
	public List<Point> boldItalicPoints(){
		return fBoldItalicPoints;
	}

}
