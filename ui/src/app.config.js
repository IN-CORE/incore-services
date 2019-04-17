let prefix = process.env.basePath === "/" ? "" : process.env.basePath;
let apiprotocol = "https";
let apihost = "incore2-services.ncsa.illinois.edu";
let apiurl = `${apiprotocol}://${apihost}`;

const config = {
	fragilityService: `${apiurl}/fragility/api/fragilities`,
	semanticService: "",
	hazardServiceBase: `${apiurl}/hazard/api/`,
	maestroService: `${apiurl}/maestro`,
	authService: `${apiurl}/auth/api/login`,
	dataServiceBase: `${apiurl}/`,
	dataService: `${apiurl}/data/api/datasets`,
	dataWolf: "https://incore2-datawolf.ncsa.illinois.edu/datawolf/",
	incoreLab: "https://incore-jupyter.ncsa.illinois.edu/",
	geoServer: "https://incore2-services.ncsa.illinois.edu/geoserver/incore/wms",
	baseUrl: process.env.basePath,
	urlPrefix: prefix
};

export default config;
