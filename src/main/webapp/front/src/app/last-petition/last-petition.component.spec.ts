import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LastPetitionComponent } from './last-petition.component';

describe('ListPetitionComponent', () => {
  let component: LastPetitionComponent;
  let fixture: ComponentFixture<LastPetitionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LastPetitionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LastPetitionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
