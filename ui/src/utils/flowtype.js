export type Dispatch = (action:any) => null;

export type AnalysisInput = {
	name: string;
	description: string;
	required: boolean;
	advanced: boolean;
	multiple: boolean;
	type: string;
};

export type AnalysisOutput = {
	name: string;
	type: string;
	description: string;
};

export type AnalysisParameter = {
	name: string;
	description: string;
	required: boolean;
	advanced: boolean;
	multiple: boolean;
	type: string;
}

export type Analysis = {
	id: string;
	description: string;
	name: string;
	category: string;
	helpContext: string;
	tag: string;
	datasets: AnalysisInput[];
	outputs: AnalysisOutput[];
	parameter: AnalysisParameter[];
};

export type Analyses = Analysis[];

export type AnalysisMetadata = {
	id: string;
	description: string;
	name: string;
	category: string;
	helpContext: string;
};

export type AnalysesMetadata = AnalysisMetadata[];

export type AnalysesState = {
	analysisMetadata: AnalysesMetadata
}


