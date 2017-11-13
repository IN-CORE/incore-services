export type Dispatch = (action:any) => null;

export type AnalysisInput = {
	id: string,
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
	id: string,
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

export type FileDescriptor = {
	id: string,
	deleted: boolean,
	filename: string,
	mimeType: string,
	size: number,
	dataURL: string,
	md5sum: string
}

export type Dataset = {
	id: string,
	deleted: boolean,
	title: string,
	description: string,
	date: Date,
	fileDescriptors: FileDescriptor[],
	contributors: string[],
	creator: string,
	type: string,
	storedUrl: string,
	format: string,
	sourceDataset: string,
	spaces: string[]
}

export type Datasets = Dataset[];

export type DatasetState = {
	datasets: Dataset[]
}

export type ExecutionState = {
	executionId: string;
}
