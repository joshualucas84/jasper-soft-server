package com.jaspersoft.jasperserver.api.metadata.common.service.impl;

import net.sf.jasperreports.engine.JasperPrint;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.PartialMock;

import java.awt.image.BufferedImage;


public class ThumbnailGeneratorTest extends UnitilsJUnit4 {

    @TestedObject
    private PartialMock<ThumbnailGenerationServiceImpl> generationService;

    private JasperPrint jasperPrint = new JasperPrint();
    private BufferedImage fakeImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);

    final int longestEdge = 300;
    final int pageHeight = 768;
    final int pageWidth = 1024;
    final float expectedZoomRatio = 299 / 1024f; // (longestEdge - 1) / max(width, height)


    @Before
    public void before() {

        // Setup JasperPrint
        jasperPrint.setPageHeight(pageHeight);
        jasperPrint.setPageWidth(pageWidth);

        // Setup ThumbnailGenerationService
        generationService.getMock().setLongestEdge(longestEdge);
        generationService.getMock().setJpgQuality(0.8f);
        generationService.returns(fakeImage).obtainImage(jasperPrint, null, expectedZoomRatio);
    }

    /**
     * This test ensures that the zoom ratio is properly calculated and used when obtaining a JasperPrint image
     */
    @Test
    public void zoomRatioCalculatedProperlyTest() {
        generationService.getMock().createThumbnail(jasperPrint, null);

        generationService.assertInvoked().createThumbnail(jasperPrint, null);
        generationService.assertInvoked().obtainImage(jasperPrint, null, expectedZoomRatio);
    }
}