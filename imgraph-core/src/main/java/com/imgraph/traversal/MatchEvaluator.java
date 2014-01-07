package com.imgraph.traversal;

/**
 * @author Aldemar Reynaga
 * Implementation of the evaluator that uses the MatchEvaluatorConf class to define
 * the conditions of the searched vertices
 */
public class MatchEvaluator implements Evaluator{

	private MatchEvaluatorConf configuration;
	
	public MatchEvaluator(MatchEvaluatorConf configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public Evaluation evaluate(ReducedVertexPath vertexPath) {
		boolean matches = false;

		
		if (configuration.getCellId() != null && vertexPath.getVertexId() == configuration.getCellId().longValue())
			matches = true;
		else {
			if (configuration.getNamePattern() != null && 
					vertexPath.getVertex().getName().matches(configuration.getNamePattern()))
				matches = true;
				
			if (configuration.getAttributePatterns() != null) {
				for (String attribute : vertexPath.getVertex().getAttributeKeys() ) {
					if (configuration.getAttributePatterns().containsKey(attribute)) {
						if (((String)vertexPath.getVertex().getAttribute(attribute))
								.matches(configuration.getAttributePatterns().get(attribute)) ) {
							matches = true;
							break;
						}
							
					}
				}
			}
		}
		
		if (matches)
			return configuration.getEvaluation();
		else 
			return Evaluation.EXCLUDE_AND_CONTINUE;
	}

}
