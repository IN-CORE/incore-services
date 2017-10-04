import React from "react";
import AnalysisSelect from "../containers/AnalysisSelect";

type Props = {
	name: string
}

const HomePage = (props: Props) => {


	return (
		<div>
			<h1>Maestro Service</h1>
			<AnalysisSelect/>
		</div>
	);
};

export default HomePage;
