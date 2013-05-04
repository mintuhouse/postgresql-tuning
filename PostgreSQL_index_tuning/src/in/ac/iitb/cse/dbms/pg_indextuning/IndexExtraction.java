package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;

import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TParseTreeNode;

public class IndexExtraction implements IExpressionVisitor
{

	private TExpression condition;
	private ArrayList<Index> indexes;
	private String ADB_NAME = null; 

	public IndexExtraction( TExpression expr , String DBName)
	{
		this.condition = expr;
		indexes = new ArrayList<Index>();
		ADB_NAME = DBName;
		
	}
	
	public ArrayList<Index> getIndexes(){
		this.condition.inOrderTraverse(this);
		//return column_names.toString().replace("[", "").replace("]", "");
		return indexes;
	}

	boolean is_compare_condition( EExpressionType t )
	{
		return ( ( t == EExpressionType.simple_comparison_t )
				|| ( t == EExpressionType.group_comparison_t ) || ( t == EExpressionType.in_t ) );
	}

	public boolean exprVisit( TParseTreeNode pnode, boolean pIsLeafNode )
	{
		TExpression lcexpr = (TExpression) pnode;
		if ( is_compare_condition( lcexpr.getExpressionType( ) ) )
		{
			TExpression leftExpr = (TExpression) lcexpr.getLeftOperand();
			String le = leftExpr.toString();
			String[] lea = le.split("\\.");
			Index in = new Index(0, "", lea[0], 0, lea[1], "", true, ADB_NAME, false);
			indexes.add(in);
		}
		return true;
	}
}
