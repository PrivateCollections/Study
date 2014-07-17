/*
 * This class is part of the book "iText in Action - 2nd Edition"
 * written by Bruno Lowagie (ISBN: 9781935182610)
 * For more info, go to: http://itextpdf.com/examples/
 * This example only works with the AGPL version of iText.
 */

package tables;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import utils.ConnectionFactory;
import utils.PojoFactory;
import beans.Movie;
import beans.Screening;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.itextpdf.text.pdf.PdfWriter;

public class AlternatingBackground implements PdfPTableEvent {

	/** The resulting PDF file. */
	public static final String RESULT = "alternating.pdf";

	/**
	 * Creates a PDF document.
	 * 
	 * @param filename
	 *            the path to the new PDF document
	 * @throws DocumentException
	 * @throws IOException
	 * @throws SQLException
	 */
	public void createPdf(String filename) throws Exception {
		// step 1
		Document document = new Document();
		// step 2
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
		writer.setEncryption("swrd".getBytes(), "swrd".getBytes(),
				PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
		writer.createXmpMetadata();
		// step 3
		document.open();
		// step 4

		// database connection and statement
		Connection conn = ConnectionFactory.getConnection();
		List<Date> days = PojoFactory.getDays(conn);
		PdfPTableEvent event = new AlternatingBackground();
		for (Date day : days) {
			PdfPTable table = getTable(conn, day);
			table.setTableEvent(event);
			document.add(table);
			document.newPage();
		}
		// step 5
		document.close();
		// Close the database connection
		conn.close();
	}

	/**
	 * Creates a table with film festival screenings.
	 * 
	 * @param connection
	 *            a database connection
	 * @param day
	 *            a film festival day
	 * @return a table with screenings.
	 * @throws SQLException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public PdfPTable getTable(Connection connection, Date day)
			throws SQLException, DocumentException, IOException {
		PdfPTable table = new PdfPTable(new float[] { 2, 1, 2, 5, 1 });
		table.setWidthPercentage(100f);
		table.getDefaultCell().setPadding(3);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(true);
		table.getDefaultCell().setColspan(5);
		table.getDefaultCell().setBackgroundColor(BaseColor.RED);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(day.toString());
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		table.getDefaultCell().setColspan(1);
		table.getDefaultCell().setBackgroundColor(BaseColor.ORANGE);
		for (int i = 0; i < 2; i++) {
			table.addCell("Location");
			table.addCell("Time");
			table.addCell("Run Length");
			table.addCell("Title");
			table.addCell("Year");
		}
		table.getDefaultCell().setBackgroundColor(null);
		table.setHeaderRows(3);
		table.setFooterRows(1);
		List<Screening> screenings = PojoFactory.getScreenings(connection, day);
		Movie movie;
		for (Screening screening : screenings) {
			movie = screening.getMovie();
			table.addCell(screening.getLocation());
			table.addCell(String.format("%1$tH:%1$tM", screening.getTime()));
			PdfPCell cell = new PdfPCell(table.getDefaultCell());
			cell.setPhrase(new Phrase(
					String.format("%d '", movie.getDuration())));
			cell.setCellEvent(new CellEvent(movie.getDuration()));
			table.addCell(cell);
			table.addCell(movie.getMovieTitle());
			table.addCell(String.valueOf(movie.getYear()));
		}
		return table;
	}

	/**
	 * Draws a background for every other row.
	 * 
	 * @see com.itextpdf.text.pdf.PdfPTableEvent#tableLayout(com.itextpdf.text.pdf.PdfPTable,
	 *      float[][], float[], int, int,
	 *      com.itextpdf.text.pdf.PdfContentByte[])
	 */
	public void tableLayout(PdfPTable table, float[][] widths, float[] heights,
			int headerRows, int rowStart, PdfContentByte[] canvases) {
		int columns;
		Rectangle rect;
		int footer = widths.length - table.getFooterRows();
		int header = table.getHeaderRows() - table.getFooterRows() + 1;
		for (int row = header; row < footer; row += 2) {
			columns = widths[row].length - 1;
			rect = new Rectangle(widths[row][0], heights[row],
					widths[row][columns], heights[row + 1]);
			rect.setBackgroundColor(BaseColor.YELLOW);
			rect.setBorder(Rectangle.NO_BORDER);
			canvases[PdfPTable.BASECANVAS].rectangle(rect);
		}
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            no arguments needed
	 * @throws DocumentException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws Exception {
		new AlternatingBackground().createPdf(RESULT);
	}

}