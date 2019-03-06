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

/* Earthquakes */
export type HazardDataset = {
	hazardType: string,
	datasetId: string,
	demandType: string,
	demandUnits: string,
	period: number,
	recurrenceInterval: number,
	recurrenceUnit: string,
	absTime:Date,
}

export type EqParameters = {
	srcLatitude: Number,
	srcLongitude: Number,
	magnitude: Number,
	coseismicRuptureDepth: Number,
	dipAngle: Number,
	azimuthAngle: Number,
	rakeAngle: Number,
	seismogenicDepth:Number,
	depth: Number,
	depth2p5KmPerSecShearWaveVelocity: Number,
	shearWaveDepth1p0: Number,
	faultTypeMap:Object,
	region:string,
}

export type Privileges = {
	userPrivileges: Object,
	groupPrivileges: Object,
}

export type VisualizationParameters = {
	demandType: string,
	demandUnits: string,
	minX: number,
	minY: number,
	maxX: number,
	maxY: number,
	numPoints: number,
	amplifyHazard: boolean,
}

export type RasterDataset = {
	hazardType: string,
	datasetId: string,
	demandType: string,
	demandUnits: string,
	period: number,
	eqParameters: EqParameters,
}

export type DeterministicEarthquake = {
	eqType: string,
	id: string,
	privileges: Privileges,
	name: string,
	description:string,
	attenuations:Object,
	eqParameters: EqParameters,
	visualizationParameters: VisualizationParameters,
	defaultSiteClass: string,
	siteAmplicfication: string,
	rasterDataset:RasterDataset,
}

export type ProbabilisticEarthquake = {
	eqType: string,
	id: string,
	privileges: Privileges,
	name:string,
	description: string,
	hazardDatasets: HazardDataset[],
}

export type Earthquake = DeterministicEarthquake | ProbabilisticEarthquake;

/* Tornado */
export type TornadoParameters = {
	efRating: string,
	maxWindSpeed: number,
	startLatitude: number,
	startLongitude: number,
	randomSeed: number,
	windSpeedMethod: number,
	numSimulations: number,
	endLatitude: number[],
	endLongitude: number[]
}

export type EfBox = {
	efBoxWidths: number[],
}

export type Tornado = {
	id: string,
	tornadoModel: string,
	tornadoParameters: TornadoParameters,
	tornadoWidth: number[],
	efBoxes: EfBox[],
	tornadoDatasetId: string,
	privileges: Privileges
}

/* Hurricane */
export type Hurricane = {
	id: string,
	privileges: Privileges,
	name: string,
	description: string,
	gridResolution: number,
	gridResolutionUnits: string,
	rasterResolution: number,
	rasterResolutionUnits: string,
	transD: number,
	transDUnits: string,
	landfallLocation: string,
	modelUsed: string,
	coast: string,
	category: number,
	velocityUnits: string,
	gridPoints: number,
	rfMethod: string,
	times: Date[],
	hazardDatasets: HazardDataset[],
}

export type Hazards = Earthquake[] | Tornado[] | Hurricane[];

export type HazardState = {
	hazards: Hazards
}

export type Datasets = Dataset[];

export type DatasetState = {
	datasets: Dataset[]
}

export type ExecutionState = {
	executionId: string;
}

export type GetState = () => Object;

export type UserState = {
	username: string,
	auth_token: string
}
