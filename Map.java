package structures;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class Map {
	private Point sstart;
	private Point sgoal;
	private Point[] httCenters;
	private Cell[][] map;
        private ArrayList<Cell> path;

	public Map() { // Generates new RANDOM Map w/o input
		map = new Cell[120][160]; // first initialize 120x160 grid

		// initialize all cells as unblocked
		for (int row = 0; row < 120; row++) {
			for (int col = 0; col < 160; col++) {
				map[row][col] = new Cell(row,col,1);
			}
		}
                
                //fills map
		placeHTTs();
		placeHighways();
                placeBlocks();
		placeStartGoal();
	}
        public Map(String file_path) throws IOException{
            path = new ArrayList<>();
            httCenters = new Point[8];
            map = new Cell[120][160]; // first initialize 120x160 grid

		// initialize all cells as unblocked
		for (int row = 0; row < 120; row++) {
			for (int col = 0; col < 160; col++) {
				map[row][col] = new Cell(row,col,1);
			}
		}

            String contents = Files.lines(Paths.get(file_path)).collect(Collectors.joining("\n"));
            String[] lines = contents.split("\n");
            sstart = strToPoint(lines[0]);
            sgoal = strToPoint(lines[1]);
            
            for(int i = 0; i < httCenters.length;i++){
                httCenters[i] = strToPoint(lines[i+2]);
            }
            for(int r = 10; r < lines.length;r++){
                for(int c = 0; c < lines[r].length(); c++){
                    Cell cell = new Cell(r-10,c,1);
                    cell.setCellType(lines[r].charAt(c));
                    map[r-10][c]= cell;
                }
            }
        }
        public void setPath(ArrayList<Cell> p){
            path = p;
        }
        public ArrayList<Cell> getPath(){
            return path;
        }
        public void setStart(Point p){
            sstart = p;
        }
        public void setStart(int x, int y){
            sstart = new Point(x,y);
        }
        public void setGoal(Point p){
            sgoal = p;
        }
        public void setGoal(int x, int y){
            sgoal = new Point(x,y);
        }
        public Cell getStart(){
            return map[sstart.x][sstart.y];
        }
        public Cell getGoal(){
            return map[sgoal.x][sgoal.y];
        }
        public Point getStartPoint(){
            return sstart;
        }
        public Point getGoalPoint(){
            return sgoal;
        }
        public Point[] getHtts(){
            return httCenters;
        }
        public Cell[][] getMap(){
            return map;
        }
        public Cell getCell(int x, int y){
            return map[x][y];
        }
        
         public void display(){
            for (int row = 0; row < 120; row++) {
			for (int col = 0; col < 160; col++) {
                                if(path.contains(map[row][col])){
                                    System.out.print("\u001B[43m"+map[row][col].cellTypeToStr()+" ");
                                }else if(sstart.equals(new Point(row,col))){
                                    System.out.print("\u001B[41m"+"S "+"\u001B[0m");
                                }else if(sgoal.equals(new Point(row,col))){
                                    System.out.print("\u001B[42m"+"G "+"\u001B[0m");
                                }else if(map[row][col].getCellType()==2){
                                    System.out.print("\u001B[45m"+"2"+"\u001B[0m"+ " ");
                                }else if(map[row][col].getCellType()==3){
                                    System.out.print("\u001B[46m"+"a"+"\u001B[0m"+ " ");
                                }else if(map[row][col].getCellType()==4){
                                    System.out.print("\u001B[44m"+"b"+"\u001B[0m"+" ");
                                }else {
                                    System.out.print(map[row][col].cellTypeToStr()+" ");
                                }
			}
			System.out.println();
		}
        }
        
        public void fileOutput(String file_name) throws IOException{
            String file = "./"+file_name+".txt";
            ArrayList<String> lines = new ArrayList<>();
            lines.add("("+(int)sstart.getX()+","+(int)sstart.getY()+")");
            lines.add("("+(int)sgoal.getX()+","+(int)sgoal.getY()+")");
            for (Point httCenter : httCenters) {
                lines.add("("+(int)httCenter.getX()+","+(int)httCenter.getY()+")");
            }
            for(int row = 0; row < map.length; row++){
                String str = new String();
                for(int col = 0;col<map[row].length; col++){
                   str = str.concat(map[row][col].cellTypeToStr());
                }
                lines.add(str);
            }
            Path write = Files.write(Paths.get(file), lines);
    
         }
        
        private Point strToPoint(String str){
            String valX = str.substring(str.indexOf("(")+1,str.indexOf(","));
            String valY = str.substring(str.indexOf(",")+1,str.indexOf(")"));
            return new Point(Integer.parseInt(valX),Integer.parseInt(valY));
        }
	private void placeHTTs() {
		httCenters = new Point[8];
		Random rand = new Random();
		for (int i = 0; i < 8;) {
			int xrand = rand.nextInt(120);
			int yrand = rand.nextInt(160);
			if (map[xrand][yrand].getCellType()==2) {
				continue;
			}
			map[xrand][yrand].setCellType(2);
			httCenters[i] = new Point(xrand, yrand); // inserts point of new htt to list of htt centers
													 
			for (int r = xrand - 15; r <= xrand + 15; r++) {
				if (r < 0 || r > 119)
					continue;

				for (int c = yrand - 15; c < yrand + 15; c++) {
					if (c < 0 || c > 119)
						continue;
					if (rand.nextBoolean()) {
						map[r][c].setCellType(2);
					}
				}
			}
			i++;
		}
	}

	private void placeHighways() {
            Cell[][] origMap = map;
            int fails=0;
            outerloop:
            for(int success = 0 ; success<4;){
                if (fails > 250) {
                    fails = 0;
                    success = 0;
                    map = origMap;
                }
                Random rand = new Random();
			int xstart, ystart;
			Point currPoint, nxtPoint;
                        ArrayList<Point> tempHway = new ArrayList();
                        //initialize the first point of the river and set direction perpendicular to border
			switch (rand.nextInt(4)) {
			case 0:
				xstart = 0;
				ystart = rand.nextInt(158) + 1;
				nxtPoint = new Point(xstart, ystart);
				nxtPoint.translate(1, 0);
				break;
			case 1:
				xstart = 119;
				ystart = rand.nextInt(158) + 1;
				nxtPoint = new Point(xstart, ystart);
				nxtPoint.translate(-1, 0);
				break;
			case 2:
				xstart = rand.nextInt(118) + 1;
				ystart = 0;
				nxtPoint = new Point(xstart, ystart);
				nxtPoint.translate(0, 1);
				break;
			case 3:
				xstart = rand.nextInt(118 + 1);
				ystart = 159;
				nxtPoint = new Point(xstart, ystart);
				nxtPoint.translate(0, -1);
				break;
			default:
				xstart = 0;
				ystart = 1;
				nxtPoint = new Point(xstart, ystart);
                                nxtPoint.translate(1, 0);
				break;
			}
			currPoint = new Point(xstart, ystart);
                        if(isHwayPoint(currPoint)){
                            continue;
                        }
                        map[currPoint.x][currPoint.y].setHighway();
                        tempHway.add(currPoint);
                        while(withinBorder(nxtPoint)){
                            if(isHwayPoint(nxtPoint)){
                                for(int i=tempHway.size()-1;i>=0;i--){
                                    Point last = tempHway.remove(i);
                                    map[last.x][last.y].removeHighway();
                                }
                                fails++;
                                continue outerloop;
                            }
                            Point temp = new Point(currPoint);
                                currPoint = new Point(nxtPoint);
                                tempHway.add(currPoint);
                                map[currPoint.x][currPoint.y].setHighway();
                                if ((tempHway.size()+1) % 20 == 0&&!atBorder(currPoint)) {
                                        if (rand.nextInt(10) < 6)
                                                nxtPoint = movStraight(temp, nxtPoint);
                                        else
                                                nxtPoint = movPerp(temp, nxtPoint);
                                } else {
                                        nxtPoint = movStraight(temp, nxtPoint);
                                }
                        }
                        if(tempHway.size()<100){
                            for(int i=tempHway.size()-1;i>=0;i--){
                                    Point last = tempHway.remove(i);
                                    map[last.x][last.y].removeHighway();
                            }
                        }else{
                           fails=0;
                            success++;
                        }
                        
            }
	}
	
	private void placeBlocks(){
		Random rand = new Random();
		for (int i = 0; i < 3840;) {
			int xrand = rand.nextInt(120);
			int yrand = rand.nextInt(160);
			if (map[xrand][yrand].getCellType()>2) {
				continue;
			}
			map[xrand][yrand].setCellType(0);
			i++;
		}
	}
        private void placeStartGoal(){
            sstart = randPoint();
            do{
                sgoal = randPoint();
            }while(sstart.distance(sgoal)<100||sstart.equals(sgoal));
           
        }
        

        private Point randPoint(){  //generates a random point within allowed regions for start and end points
            Random rand = new Random();
            int x,y, i;
            i = rand.nextInt(4);
            switch(i){
                case 0: //Top 20 rows
                    x = rand.nextInt(120);
                    y = rand.nextInt(20);
                    break;
                case 1: //Bottom 20 rows
                    x = rand.nextInt(120);
                    y = rand.nextInt(20)+140;
                    break;
                case 3: //left most 20
                    x = rand.nextInt(20);
                    y = rand.nextInt(160);
                    break;
                case 4: //right most 20
                    x = rand.nextInt(20)+100;
                    y = rand.nextInt(160);
                    break;
                default:
                    x = 0;
                    y = 0;
                    break;
            }
            return new Point(x,y);
        }   
	private boolean northward(Point curr, Point nxt) {
		return (curr.getX() > nxt.getX() && curr.getY() == nxt.getY());
			
	}

	private boolean southward(Point curr, Point nxt) {
		return (curr.getX() < nxt.getX() && curr.getY() == nxt.getY());
	}

	private boolean eastward(Point curr, Point nxt) {
		return (curr.getX() == nxt.getX() && curr.getY() < nxt.getY());
	}

	private boolean westward(Point curr, Point nxt) {
		return (curr.getX() == nxt.getX() && curr.getY() > nxt.getY());
	}

	private Point movStraight(Point curr, Point nxt) {
		if (northward(curr, nxt)) {
			nxt.translate(-1, 0);
		}
		if (southward(curr, nxt)) {
			nxt.translate(1, 0);
		}
		if (eastward(curr, nxt)) {
			nxt.translate(0, 1);
		}
		if (westward(curr, nxt)) {
			nxt.translate(0, -1);
		}
		return nxt;
	}

	private Point movPerp(Point curr, Point nxt) {
		Random rand = new Random();
		if (rand.nextBoolean()) {
			return movLeft(curr, nxt);
		} else
			return movRight(curr, nxt);
	}

	private Point movLeft(Point curr, Point nxt) {
		if (northward(curr, nxt)) {
			nxt.translate(0, 1);
		}
		if (southward(curr, nxt)) {
			nxt.translate(0, -1);
		}
		if (eastward(curr, nxt)) {
			nxt.translate(-1, 0);
		}
		if (westward(curr, nxt)) {
			nxt.translate(1, 0);
		}
		return nxt;
	}

	private Point movRight(Point curr, Point nxt) {
		if (northward(curr, nxt)) {
			nxt.translate(0, -1);
		}
		if (southward(curr, nxt)) {
			nxt.translate(0, 1);
		}
		if (eastward(curr, nxt)) {
			nxt.translate(1, 0);
		}
		if (westward(curr, nxt)) {
			nxt.translate(-1, 0);
		}
		return nxt;
	}

	private boolean isHwayPoint(Point p) {
		return map[(int) p.getX()][(int) p.getY()].getCellType()==3||map[(int) p.getX()][(int) p.getY()].getCellType()==4;
	}

	public boolean withinBorder(Point p) {
		return (p.x >= 0 && p.x < 120 && p.y >= 0 && p.y < 160);
	}

	private boolean atBorder(Point p) {
		return (withinBorder(p) && (p.x == 0 || p.x == 119 || p.y == 0 || p.y == 159));
	}

}

