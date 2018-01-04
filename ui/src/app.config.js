const devConfig = {
	basePath: "/",
	fragilityService: "http://localhost:8080/fragility/api/fragilities",
	semanticService: "",
	hazardService: "http://141.142.210.193:8888/hazard/api/earthquakes/",
	maestroService: "http://localhost:8080",
	authService: "https://incore2-services.ncsa.illinois.edu/auth/api/login",
	dataServiceBase: "http://141.142.210.193:8888/",
	dataService: "http://141.142.210.193:8888/data/api/datasets",
	dataWolf: "http://141.142.209.63/datawolf/"
};

const prodConfig = {
	basePath: "/",
	fragilityService: "https://incore2-services.ncsa.illinois.edu/fragility/api/fragilities",
	semanticService: "",
	hazardService: "https:/incore2-services.ncsa.illinois.edu//hazard/api/earthquakes/",
	maestroService: "https:/incore2-services.ncsa.illinois.edu/maestro/",
	authService: "https://incore2-services.ncsa.illinois.edu/auth/api/login",
	dataServiceBase: "https:/incore2-services.ncsa.illinois.edu:8888/",
	dataService: "https:/incore2-services.ncsa.illinois.edu:8888/data/api/datasets",
	dataWolf: "https:/incore2-datawolf.ncsa.illinois.edu:8888/"
};

const config = getConfig();

function getConfig() {
	if (process.env.NODE_ENV === "production") {
		return prodConfig;
	} else {
		return devConfig;
	}
}

export default config;
