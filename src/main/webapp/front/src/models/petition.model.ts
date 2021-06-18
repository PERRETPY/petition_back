import {PropertiesPetition} from './properties.petition.model';
import {KeyPetition} from './key.petition.model';

export class Petition {

  constructor(public key: KeyPetition,
              public properties: PropertiesPetition) {
  }
}
