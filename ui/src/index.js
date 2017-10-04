// Set up your application entry point here...
///* eslint-disable import/default */

import React from "react";
import { render } from "react-dom";
import { Provider } from "react-redux";
import { Router, browserHistory } from "react-router";

import routes from "./routes";
import configureStore from "./store/configureStore";

import "./styles/styles.scss";
import { syncHistoryWithStore } from "react-router-redux";

require("./public/favicon.ico");

const store = configureStore();

// Create an enhanced history that syncs navigation events with the store
const history = syncHistoryWithStore(browserHistory, store);

render(
	<Provider store={store}>
		<Router history={history} routes={routes} />
	</Provider>, document.getElementById("app")
);
