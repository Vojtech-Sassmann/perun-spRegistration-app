import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Facility } from "../../core/models/Facility";
import { FacilitiesService } from "../../core/services/facilities.service";
import { Subscription } from "rxjs";
import {MatSort, MatTableDataSource} from "@angular/material";

@Component({
  selector: 'app-facilities-overview',
  templateUrl: './facilities-overview.component.html',
  styleUrls: ['./facilities-overview.component.scss']
})
export class FacilitiesOverviewComponent implements OnInit, OnDestroy {

  constructor(private facilitiesService: FacilitiesService) { }

  @Input()
  facilities: Facility[] = [
    {
      id: 1,
      name: "C Cloud META 9",
      description: "Very ugly and old facility",
      attrs: []
    },
    {
      id: 2,
      name: "A Cloud",
      description: "Very ugly and old facility and this is the only facility that uses the unordinary black papers for their customers. ",
      attrs: []
    },
    {
      id: 3,
      name: "X Cloud META",
      description: "Very ugly and old facility",
      attrs: []
    }
  ];

  @ViewChild(MatSort) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  displayedColumns: string[] = ['id', 'name'];
  dataSource: MatTableDataSource<Facility>;
  loading = true;

  private sort: MatSort;
  private facilitiesSubscription: Subscription;

  setDataSource() {
    if (!!this.dataSource) {
      this.dataSource.sort = this.sort;
    }
  }

  ngOnInit() {
    this.facilitiesSubscription = this.facilitiesService.getMyFacilities().subscribe(facilities => {
      // TODO: uncomment this
      // this.facilities = facilities;
      this.dataSource = new MatTableDataSource<Facility>(this.facilities);
      this.dataSource.sort = this.sort;
      this.loading = false;
    });
  }

  ngOnDestroy() {
    this.facilitiesSubscription.unsubscribe();
  }
}
