let prefix = process.env.basePath === "/" ? "" : process.env.basePath;

const config = {
	fragilityService: "https://incore2-services.ncsa.illinois.edu/fragility/api/fragilities",
	semanticService: "",
	hazardServiceBase: "https://incore2-services.ncsa.illinois.edu/hazard/api/",
	maestroService: "https://incore2-services.ncsa.illinois.edu/maestro",
	authService: "https://incore2-services.ncsa.illinois.edu/auth/api/login",
	dataServiceBase: "https://incore2-services.ncsa.illinois.edu/",
	dataService: "https://incore2-services.ncsa.illinois.edu/data/api/datasets",
	dataWolf: "https://incore2-datawolf.ncsa.illinois.edu/datawolf/",
	incoreLab: "https://incore-jupyter.ncsa.illinois.edu/",
	geoServer: "https://incore2-services.ncsa.illinois.edu/geoserver/incore/wms",
	baseUrl: process.env.basePath,
	urlPrefix: prefix
};

export default config;
