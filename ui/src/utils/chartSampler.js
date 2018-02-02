import alegbra from "algebra.js";
import math from "mathjs";
import { jStat } from "jstat";

export default class chartSampler {

	static computeExpressionSamples(min, max, numberOfSamples, expression) {
		let steps = ((max - min) / numberOfSamples);

		let samples = [];

		for (let i = 1; i <= numberOfSamples; i++) {
			let y = steps * i;
			let equation = alegbra.parse(`${y} = ${expression}`);

			let x = equation.solveFor("x").valueOf();

			samples.push([x, y]);
		}

		return samples;
	}

	static async computeExpressionSamples3d(minX, maxX, numberOfSamplesX, minY, maxY, numberOfSamplesY, expression) {
		let stepsX = ((maxX - minX) / numberOfSamplesX);
		let stepsY = ((maxY - minY) / numberOfSamplesY);

		let parser = math.parser();

		parser.eval(`f(x,y) = ${  expression}`);

		let promises = [];

		for (let i = 1; i <= numberOfSamplesX; i++) {
			let x = stepsX * i;

			for (let j = 1; j <= numberOfSamplesY; j++) {
				let y = stepsY * j;

				promises.push(this.calculate(x, y, parser));
			}
		}

		let samples = await Promise.all(promises);

		return samples;
	}

	static async calculate(x, y, parser) {
		return new Promise(resolve => resolve([x, y, parser.eval(`f(${  x  }, ${  y  })`)]));
	}

	//static computeExpressionSamples3d(minX, maxX, numberOfSamplesX, minY, maxY, numberOfSamplesY, expression) {
	//  let stepsX = ((maxX - minX) / numberOfSamplesX);
	//  let stepsY = ((maxY - minY) / numberOfSamplesY);
	//
	//  let parser = math.parser();
	//  let samples = [];
	//
	//  parser.eval("f(x,y) = " + expression);
	//
	//  for (let i = 1; i <= numberOfSamplesX; i++) {
	//    let x = stepsX * i;
	//
	//    for (let j = 1; j <= numberOfSamplesY; j++) {
	//      let y = stepsY * j;
	//
	//      let z = parser.eval("f(" + x + ", " + y + ")");
	//
	//      samples.push([x, y, z]);
	//    }
	//  }
	//
	//  return samples;
	//}

	static sampleNormalCdf(min, max, numberOfSamples, mean, std) {
		let steps = ((max - min) / numberOfSamples);

		let samples = [];

		for (let i = 1; i <= numberOfSamples; i++) {
			let x = steps * i;

			let y = jStat.normal.inv(x, mean, std);

			samples.push([y, x]);
		}

		return samples;
	}

	static sampleLogNormalAlternate(min, max, numberOfSamples, mean, std) {
		let steps = ((max - min) / numberOfSamples);

		let samples = [];

		for (let i = 1; i <= numberOfSamples; i++) {
			let x = steps * i;

			let y = jStat.lognormal.inv(x, mean, std);

			samples.push([y, x]);
		}

		return samples;

	}

	static sampleLogNormalCdf(min, max, numberOfSamples, location, scale) {
		let steps = ((max - min) / numberOfSamples);

		let samples = [];

		let mean = Math.log(Math.pow(location,2) / Math.sqrt(scale + Math.pow(location,2)));
		let std = Math.sqrt(Math.log((scale / Math.pow(location, 2)) + 1 ));

		for (let i = 1; i <= numberOfSamples; i++) {
			let x = steps * i;

			let y = jStat.lognormal.inv(x, mean, std);

			samples.push([y, x]);
		}

		return samples;
	}

	//static computeLogNormalCdfSamplesYAxis(min, max, numberOfSamples, mean, std) {
	//	let steps = ((max - min) / numberOfSamples);
	//
	//	let samples = [];
	//
	//	for (let i = 1; i <= numberOfSamples; i++) {
	//		let x = steps * i;
	//
	//		let y = jStat.lognormal.cdf(x, mean, std);
	//
	//		samples.push([x, y]);
	//	}
	//
	//	return samples;
	//}

	//static computeLogNormalCdfSamples(numberOfSamples, mean, std) {
	//	let min = 0;
	//	let max = 0; // value at which is 1
	//	let steps = ((max - min) / numberOfSamples);
	//
	//	let samples = [];
	//
	//	for (let i = 1; i <= numberOfSamples; i++) {
	//		let x = steps * i;
	//
	//		let y = this.logNormalCdf(x, mean, std);
	//
	//		samples.push([x, y]);
	//	}
	//
	//	return samples;
	//}

	//static normalCdf(x, mean, std) {
	//	return 0.5 * (1 + math.erf((x - mean) / (Math.sqrt(2) * std)));
	//}

	//
	//static logNormalCdf(x, mean, std) {
	//  return 0.5 + (0.5 * math.erf((Math.log(x) - mean) / (Math.sqrt(2) * std)));
	//}
	//

	//static logNormalCdf(x, mean, std) {
	//	return this.normalCdf(Math.log(x), mean, std);
	//}

	//static erf(x) {
	//  // save the sign of x
	//  let sign = (x >= 0) ? 1 : -1;
	//  x = Math.abs(x);
	//
	//  // constants
	//  let a1 = 0.254829592;
	//  let a2 = -0.284496736;
	//  let a3 = 1.421413741;
	//  let a4 = -1.453152027;
	//  let a5 = 1.061405429;
	//  let p = 0.3275911;
	//
	//  // A&S formula 7.1.26
	//  let t = 1.0 / (1.0 + p * x);
	//  let y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
	//
	//  return sign * y; // erf(-x) = -erf(x);
	//}
}
