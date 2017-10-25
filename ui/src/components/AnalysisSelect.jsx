import React, {Component} from "react";
import SelectField from "material-ui";

class AnalysisSelect extends Component {

	constructor(props) {
		super(props);
		this.state = {};
		this.onChange = this.onChange.bind(this);
	}
	onChange(event: Object) {
		this.props.onSelectAnalysis(event.target.value);
	}

	render() {
		const options = this.props.analyses.map( d =>
			<option value={d.id} key={d.id} > {d.name}</option>
		);

		return (
			<div>
				<SelectField onChange={this.onChange}>
					{options}
				</SelectField >
			</div>
		);
	}

}

export default AnalysisSelect;
