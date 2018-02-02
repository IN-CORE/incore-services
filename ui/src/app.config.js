const devConfig = {
	basePath: "/",
	fragilityService: "http://localhost:8080/fragility/api/fragilities",
	semanticService: "",
	hazardService: "https://incore2-services.ncsa.illinois.edu/hazard/api/earthquakes/",
	maestroService: "http://localhost:8080/maestro",
	authService: "https://incore2-services.ncsa.illinois.edu/auth/api/login",
	dataServiceBase: "https://incore2-services.ncsa.illinois.edu/",
	dataService: "https://incore2-services.ncsa.illinois.edu/data/api/datasets",
	dataWolf: "https://141.142.209.63/datawolf/"
};

const prodConfig = {
	basePath: "/",
	fragilityService: "https://incore2-services.ncsa.illinois.edu/fragility/api/fragilities",
	semanticService: "",
	hazardService: "https://incore2-services.ncsa.illinois.edu/hazard/api/earthquakes/",
	maestroService: "https://incore2-services.ncsa.illinois.edu/maestro",
	authService: "https://incore2-services.ncsa.illinois.edu/auth/api/login",
	dataServiceBase: "https://incore2-services.ncsa.illinois.edu/",
	dataService: "https://incore2-services.ncsa.illinois.edu/data/api/datasets",
	dataWolf: "https://incore2-datawolf.ncsa.illinois.edu/datawolf/"
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
