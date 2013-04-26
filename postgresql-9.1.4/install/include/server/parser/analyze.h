/*-------------------------------------------------------------------------
 *
 * analyze.h
 *		parse analysis for optimizable statements
 *
 *
 * Portions Copyright (c) 1996-2011, PostgreSQL Global Development Group
 * Portions Copyright (c) 1994, Regents of the University of California
 *
 * src/include/parser/analyze.h
 *
 *-------------------------------------------------------------------------
 */
#ifndef ANALYZE_H
#define ANALYZE_H

#include "parser/parse_node.h"

/**
 * HYPOTHETICAL INDEX
 * SELF TUNING GROUP - PUC-RIO - 2010
 *
 * We are defining the parse_analyze function with one more attribute to
 * represents hypothetical indexes.
 */
extern Query *parse_analyze(Node *parseTree, const char *sourceText,
			  Oid *paramTypes, int numParams, bool hypothetical);
extern Query *parse_analyze_varparams(Node *parseTree, const char *sourceText,
						Oid **paramTypes, int *numParams);

extern Query *parse_sub_analyze(Node *parseTree, ParseState *parentParseState,
				  CommonTableExpr *parentCTE,
				  bool locked_from_parent);
/**
 * HYPOTHETICAL INDEX
 * SELF TUNING GROUP - PUC-RIO - 2010
 *
 * We are defining the transformStmt function with one more attribute to
 * represents hypothetical indexes.
 */
extern Query *transformStmt(ParseState *pstate, Node *parseTree, bool hypothetical); /* HYPOTHETICAL INDEX SELF TUNING GROUP - PUC-RIO - 2010 */

extern bool analyze_requires_snapshot(Node *parseTree);

extern void CheckSelectLocking(Query *qry);
extern void applyLockingClause(Query *qry, Index rtindex,
				   bool forUpdate, bool noWait, bool pushedDown);

#endif   /* ANALYZE_H */
