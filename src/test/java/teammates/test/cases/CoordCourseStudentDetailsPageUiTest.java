package teammates.test.cases;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.common.Common;
import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.StudentData;
import teammates.test.driver.BackDoor;
import teammates.test.driver.BrowserInstance;
import teammates.test.driver.BrowserInstancePool;
import teammates.test.driver.TestProperties;

/**
 * Tests Coordinator Course Student Details and Edit UI
 */
public class CoordCourseStudentDetailsPageUiTest extends BaseTestCase {
	private static BrowserInstance bi;
	private static DataBundle scn;
	
	private static String appUrl = TestProperties.inst().TEAMMATES_URL;

	@BeforeClass
	public static void classSetup() throws Exception {
		printTestClassHeader();
		
		startRecordingTimeForDataImport();
		String jsonString = Common.readFile(Common.TEST_DATA_FOLDER+"/CoordCourseStudentDetailsUiTest.json");
		scn = Common.getTeammatesGson().fromJson(jsonString, DataBundle.class);
		BackDoor.deleteCoordinators(jsonString);
		String backDoorOperationStatus = BackDoor.persistNewDataBundle(jsonString);
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
		reportTimeForDataImport();
		
		bi = BrowserInstancePool.getBrowserInstance();
		
		bi.loginAdmin(TestProperties.inst().TEST_ADMIN_ACCOUNT, TestProperties.inst().TEST_ADMIN_PASSWORD);
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserInstancePool.release(bi);
		printTestClassFooter();
	}
	
	@Test
	public void testCoordCourseStudentDetailsPage() throws Exception{
		
		
		______TS("view registered student");
		
		String link = appUrl+Common.PAGE_COORD_COURSE_STUDENT_DETAILS;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,scn.courses.get("CCSDetailsUiT.CS2104").id);
		link = Common.addParamToUrl(link,Common.PARAM_STUDENT_EMAIL,scn.students.get("registeredStudent").email);
		link = Common.addParamToUrl(link,Common.PARAM_USER_ID,scn.coords.get("teammates.test").id);
		bi.goToUrl(link);
		
		bi.verifyCurrentPageHTMLRegex(Common.TEST_PAGES_FOLDER+"/coordCourseStudentDetailsPage.html");

		______TS("view unregistered student");
		
		link = appUrl+Common.PAGE_COORD_COURSE_STUDENT_DETAILS;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,scn.courses.get("CCSDetailsUiT.CS2104").id);
		link = Common.addParamToUrl(link,Common.PARAM_STUDENT_EMAIL,scn.students.get("unregisteredStudent").email);
		link = Common.addParamToUrl(link,Common.PARAM_USER_ID,scn.coords.get("teammates.test").id);
		bi.goToUrl(link);
		
		bi.verifyCurrentPageHTMLRegex(Common.TEST_PAGES_FOLDER+"/coordCourseStudentDetailsUnregisteredPage.html");
	}
	
	@Test
	public void testCoordCourseStudentEditPage() throws Exception{
		
		
		______TS("edit unregistered student");
		
		String link = appUrl+Common.PAGE_COORD_COURSE_STUDENT_EDIT;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,scn.courses.get("CCSDetailsUiT.CS2104").id);
		link = Common.addParamToUrl(link,Common.PARAM_STUDENT_EMAIL,scn.students.get("unregisteredStudent").email);
		link = Common.addParamToUrl(link,Common.PARAM_USER_ID,scn.coords.get("teammates.test").id);
		bi.goToUrl(link);
		
		bi.verifyCurrentPageHTMLRegex(Common.TEST_PAGES_FOLDER+"/coordCourseStudentEditUnregisteredPage.html");

		______TS("edit registered student");

		link = appUrl+Common.PAGE_COORD_COURSE_STUDENT_EDIT;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,scn.courses.get("CCSDetailsUiT.CS2104").id);
		link = Common.addParamToUrl(link,Common.PARAM_STUDENT_EMAIL,scn.students.get("registeredStudent").email);
		link = Common.addParamToUrl(link,Common.PARAM_USER_ID,scn.coords.get("teammates.test").id);
		bi.goToUrl(link);
		
		//check the default view
		bi.verifyCurrentPageHTMLRegex(Common.TEST_PAGES_FOLDER+"/coordCourseStudentEditPage.html");
		
		// Edit details wrongly (empty field)
		bi.fillString(bi.studentDetailTeam, "");
		bi.click(bi.coordCourseDetailsStudentEditSaveButton);						
		// Verify status message
		bi.waitForStatusMessage("Please fill in all the relevant fields.");
		
		// Edit details wrongly (invalid name)
		bi.fillString(bi.studentDetailName, "nameshouldhavelessthan40characters,andonlyalphanumeric");
		bi.fillString(bi.studentDetailTeam, "New teamname");
		bi.click(bi.coordCourseDetailsStudentEditSaveButton);								
		// Verify status message
		bi.waitForStatusMessage("Name should only consist of alphanumerics and not\nmore than 40 characters.");
				
		// Edit details wrongly (invalid teamname)
		bi.fillString(bi.studentDetailName, "New name");
		bi.fillString(bi.studentDetailTeam, "teamnameshouldhavelessthan25characters");
		bi.click(bi.coordCourseDetailsStudentEditSaveButton);								
		// Verify status message
		bi.waitForStatusMessage("Team name should contain less than 25 characters.");

		// Edit details wrongly (invalid email)
		bi.fillString(bi.studentDetailTeam, "new teamname");
		bi.fillString(bi.studentDetailNewEmail, "invalidemail");
		bi.click(bi.coordCourseDetailsStudentEditSaveButton);										
		// Verify status message
		bi.waitForStatusMessage("The e-mail address is invalid.");
		
		// Edit details correctly
		bi.fillString(bi.studentDetailName, "New name");
		bi.fillString(bi.studentDetailTeam, "New team");
		bi.fillString(bi.studentDetailNewEmail, "newemail@gmail.com");
		bi.fillString(bi.studentDetailComment, "New comments");
		bi.click(bi.coordCourseDetailsStudentEditSaveButton);
				
		// Verify status message
		bi.waitForStatusMessage(Common.MESSAGE_STUDENT_EDITED);
				
		// Verify data
		String json = BackDoor.getStudentAsJson(scn.courses.get("CCSDetailsUiT.CS2104").id, "newemail@gmail.com");
		StudentData student = Common.getTeammatesGson().fromJson(json, StudentData.class);
		assertEquals("New name",student.name);
		assertEquals("New team",student.team);
		assertEquals(scn.students.get("registeredStudent").id,student.id);
		assertEquals("newemail@gmail.com",student.email);
		assertEquals("New comments",student.comments);
	}
}